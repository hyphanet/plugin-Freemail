/*
 * Channel.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007 Alexander Lehmann
 * Copyright (C) 2009 Martin Nyhus
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package freemail.transport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.archive.util.Base32;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.RSAKeyParameters;

import freemail.AccountManager;
import freemail.Freemail;
import freemail.FreemailAccount;
import freemail.MessageBank;
import freemail.Postman;
import freemail.SlotManager;
import freemail.SlotSaveCallback;
import freemail.fcp.ConnectionTerminatedException;
import freemail.fcp.FCPBadFileException;
import freemail.fcp.FCPFetchException;
import freemail.fcp.FCPInsertErrorMessage;
import freemail.fcp.HighLevelFCPClient;
import freemail.fcp.SSKKeyPair;
import freemail.utils.DateStringFactory;
import freemail.utils.Logger;
import freemail.utils.PropsFile;
import freemail.wot.Identity;
import freemail.wot.WoTConnection;

//FIXME: The message id gives away how many messages has been sent over the channel.
//       Could it be replaced by a different solution that gives away less information?
public class Channel extends Postman {
	private static final String CHANNEL_PROPS_NAME = "props";
	private static final int POLL_AHEAD = 6;
	private static final String OUTBOX_DIR_NAME = "outbox";
	private static final String INDEX_NAME = "index";

	//The keys used in the props file
	private static class PropsKeys {
		private static final String PRIVATE_KEY = "privateKey";
		private static final String PUBLIC_KEY = "publicKey";
		private static final String FETCH_SLOT = "fetchSlot";
		private static final String SEND_SLOT = "sendSlot";
		private static final String MESSAGE_ID = "messageId";
		private static final String SENDER_STATE = "sender-state";
		private static final String RECIPIENT_STATE = "recipient-state";
		private static final String RTS_SENT_AT = "rts-sent-at";
		private static final String SEND_CODE = "sendCode";
		private static final String FETCH_CODE = "fetchCode";
	}

	private final File channelDir;
	private final PropsFile channelProps;
	private final ScheduledExecutorService executor;
	private final HighLevelFCPClient fcpClient;
	private final Freemail freemail;
	private final FreemailAccount account;
	private final Fetcher fetcher = new Fetcher();
	private final Sender sender = new Sender();
	private final PropsFile messageIndex;

	public Channel(File channelDir, ScheduledExecutorService executor, HighLevelFCPClient fcpClient, Freemail freemail, FreemailAccount account) {
		if(executor == null) throw new NullPointerException();
		this.executor = executor;

		this.fcpClient = fcpClient;
		this.account = account;

		if(freemail == null) throw new NullPointerException();
		this.freemail = freemail;

		assert channelDir.isDirectory();
		this.channelDir = channelDir;

		File channelPropsFile = new File(channelDir, CHANNEL_PROPS_NAME);
		if(!channelPropsFile.exists()) {
			try {
				if(!channelPropsFile.createNewFile()) {
					Logger.error(this, "Could not create new props file in " + channelDir);
				}
			} catch(IOException e) {
				Logger.error(this, "Could not create new props file in " + channelDir);
			}
		}
		channelProps = PropsFile.createPropsFile(channelPropsFile);

		//Message id is needed for queuing messages so it must be present
		if(channelProps.get(PropsKeys.MESSAGE_ID) == null) {
			channelProps.put(PropsKeys.MESSAGE_ID, "0");
		}

		File outbox = new File(channelDir, OUTBOX_DIR_NAME);
		File indexFile = new File(outbox, INDEX_NAME);
		messageIndex = PropsFile.createPropsFile(indexFile);
	}

	public void processRTS(PropsFile rtsProps) {
		Logger.debug(this, "Processing RTS");

		//TODO: Set public key using private key
		//TODO: Handle cases where we get a second RTS

		synchronized(channelProps) {
			if(channelProps.get(PropsKeys.PRIVATE_KEY) == null) {
				channelProps.put(PropsKeys.PRIVATE_KEY, rtsProps.get("channel"));
			}

			channelProps.put(PropsKeys.FETCH_SLOT, rtsProps.get("initiatorSlot"));
			channelProps.put(PropsKeys.FETCH_CODE, "i");

			if(channelProps.get(PropsKeys.SEND_CODE) == null) {
				channelProps.put(PropsKeys.SEND_CODE, "r");
			}

			if(channelProps.get(PropsKeys.SEND_SLOT) == null) {
				channelProps.put(PropsKeys.SEND_SLOT, rtsProps.get("responderSlot"));
			}

			if(channelProps.get(PropsKeys.MESSAGE_ID) == null) {
				channelProps.put(PropsKeys.MESSAGE_ID, "0");
			}

			if(channelProps.get(PropsKeys.RECIPIENT_STATE) == null) {
				channelProps.put(PropsKeys.RECIPIENT_STATE, "rts-received");
			}
		}

		//Queue the CTS insert
		queueCTS();
		startTasks();
	}

	//TODO: This is mostly duplicated from sendMessage
	private void queueCTS() {
		long messageId;
		synchronized(channelProps) {
			messageId = Long.parseLong(channelProps.get(PropsKeys.MESSAGE_ID));
			channelProps.put(PropsKeys.MESSAGE_ID, messageId + 1);
		}

		//Write the message and attributes to the outbox
		File outbox = new File(channelDir, OUTBOX_DIR_NAME);
		if(!outbox.exists()) {
			if(!outbox.mkdir()) {
				Logger.error(this, "Couldn't create outbox directory: " + outbox);
				return;
			}
		}

		File messageFile = new File(outbox, "" + messageId);
		if(messageFile.exists()) {
			//TODO: Pick next message id?
			Logger.error(this, "Message id already in use");
			return;
		}

		try {
			if(!messageFile.createNewFile()) {
				Logger.error(this, "Couldn't create message file: " + messageFile);
				return;
			}

			synchronized(messageIndex) {
				messageIndex.put(messageId + ".status", "unsent");
			}

			OutputStream os = new FileOutputStream(messageFile);
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

			//Then what will be the header of the inserted message
			pw.print("messagetype=cts\r\n");
			pw.print("\r\n");
			pw.flush();
		} catch(IOException e) {
			if(messageFile.exists()) {
				if(!messageFile.delete()) {
					Logger.error(this, "Couldn't delete message file (" + messageFile + ") after IOException");
				}
			}

			return;
		}

		sender.execute();

		return;
	}

	//TODO: The subtasks must support being called multiple times
	public void startTasks() {
		startFetcher();
		startSender();
		startRTSSender();
	}

	private void startFetcher() {
		//Start fetcher if possible
		String fetchSlot;
		String fetchCode;
		String publicKey;
		synchronized(channelProps) {
			fetchSlot = channelProps.get(PropsKeys.FETCH_SLOT);
			fetchCode = channelProps.get(PropsKeys.FETCH_CODE);
			publicKey = channelProps.get(PropsKeys.PUBLIC_KEY);
		}

		if((fetchSlot != null) && (fetchCode != null) && (publicKey != null)) {
			fetcher.execute();
		}
	}

	private void startSender() {
		//Start sender if possible
		String sendSlot;
		String sendCode;
		String privateKey;
		synchronized(channelProps) {
			sendSlot = channelProps.get(PropsKeys.SEND_SLOT);
			sendCode = channelProps.get(PropsKeys.SEND_CODE);
			privateKey = channelProps.get(PropsKeys.PRIVATE_KEY);
		}

		if((sendSlot != null) && (sendCode != null) && (privateKey != null)) {
			sender.execute();
		}
	}

	private void startRTSSender() {
		String state;
		synchronized(channelProps) {
			state = channelProps.get(PropsKeys.SENDER_STATE);
		}

		if((state != null) && state.equals("cts-received")) {
			return;
		}

		new RTSSender().execute();
	}

	/**
	 * Places the data read from {@code message} on the send queue
	 * @param message the data to be sent
	 * @return {@code true} if the message was placed on the queue
	 * @throws NullPointerException if {@code message} is {@code null}
	 */
	public boolean sendMessage(InputStream message) {
		if(message == null) throw new NullPointerException("Parameter message was null");

		long messageId;
		synchronized(channelProps) {
			messageId = Long.parseLong(channelProps.get(PropsKeys.MESSAGE_ID));
			channelProps.put(PropsKeys.MESSAGE_ID, messageId + 1);
		}

		//Write the message and attributes to the outbox
		File outbox = new File(channelDir, OUTBOX_DIR_NAME);
		if(!outbox.exists()) {
			if(!outbox.mkdir()) {
				Logger.error(this, "Couldn't create outbox directory: " + outbox);
				return false;
			}
		}

		File messageFile = new File(outbox, "" + messageId);
		if(messageFile.exists()) {
			//TODO: Pick next message id?
			Logger.error(this, "Message id already in use");
			return false;
		}

		try {
			if(!messageFile.createNewFile()) {
				Logger.error(this, "Couldn't create message file: " + messageFile);
				return false;
			}

			synchronized(messageIndex) {
				messageIndex.put(messageId + ".status", "unsent");
			}

			OutputStream os = new FileOutputStream(messageFile);
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

			//Then what will be the header of the inserted message
			pw.print("messagetype=message\r\n");
			pw.print("id=" + messageId + "\r\n");
			pw.print("\r\n");
			pw.flush();

			//And the message contents
			byte[] buffer = new byte[1024];
			while(true) {
				int count = message.read(buffer, 0, buffer.length);
				if(count == -1) break;

				os.write(buffer, 0, count);
			}
		} catch(IOException e) {
			if(messageFile.exists()) {
				if(!messageFile.delete()) {
					Logger.error(this, "Couldn't delete message file (" + messageFile + ") after IOException");
				}
			}

			return false;
		}

		sender.execute();

		return true;
	}

	private String calculateNextSlot(String slot) {
		byte[] buf = Base32.decode(slot);
		SHA256Digest sha256 = new SHA256Digest();
		sha256.update(buf, 0, buf.length);
		sha256.doFinal(buf, 0);

		return Base32.encode(buf);
	}

	private String generateRandomSlot() {
		SHA256Digest sha256 = new SHA256Digest();
		byte[] buf = new byte[sha256.getDigestSize()];
		SecureRandom rnd = new SecureRandom();
		rnd.nextBytes(buf);
		return Base32.encode(buf);
	}

	private class Fetcher implements Runnable {
		@Override
		public synchronized void run() {
			Logger.debug(this, "Fetcher running");

			try {
				realRun();
			} catch(RuntimeException e) {
				Logger.debug(this, "Caugth " + e);
				e.printStackTrace();
				throw e;
			} catch(Error e) {
				Logger.debug(this, "Caugth " + e);
				e.printStackTrace();
				throw e;
			}
		}

		private void realRun() {
			String slots;
			synchronized(channelProps) {
				slots = channelProps.get(PropsKeys.FETCH_SLOT);
			}
			if(slots == null) {
				Logger.error(this, "Channel " + channelDir.getName() + " is corrupt - account file has no '" + PropsKeys.FETCH_SLOT + "' entry!");
				//TODO: Either delete the channel or resend the RTS
				return;
			}

			HashSlotManager slotManager = new HashSlotManager(new ChannelSlotSaveImpl(channelProps, PropsKeys.FETCH_SLOT), null, slots);
			slotManager.setPollAhead(POLL_AHEAD);

			String basekey;
			synchronized(channelProps) {
				basekey = channelProps.get(PropsKeys.PUBLIC_KEY);
			}
			if(basekey == null) {
				Logger.error(this, "Contact " + channelDir.getName() + " is corrupt - account file has no '" + PropsKeys.PRIVATE_KEY + "' entry!");
				//TODO: Either delete the channel or resend the RTS
				return;
			}

			String fetchCode;
			synchronized(channelProps) {
				fetchCode = channelProps.get(PropsKeys.FETCH_CODE);
			}

			if(fetchCode == null) {
				Logger.error(this, "Contact " + channelDir.getName() + " is corrupt - account file has no '" + PropsKeys.FETCH_CODE + "' entry!");
				//TODO: Either delete the channel or resend the RTS
				return;
			}
			basekey += fetchCode + "-";

			String slot;
			while((slot = slotManager.getNextSlot()) != null) {
				String key = basekey + slot;

				Logger.minor(this, "Attempting to fetch mail on key " + key);
				File result;
				try {
					result = fcpClient.fetch(key);
				} catch(ConnectionTerminatedException e) {
					Logger.debug(this, "Connection terminated");
					return;
				} catch(FCPFetchException e) {
					if(e.isFatal()) {
						Logger.normal(this, "Fatal fetch failure, marking slot as used");
						slotManager.slotUsed();
					}

					Logger.minor(this, "No mail in slot (fetch returned " + e.getMessage() + ")");
					continue;
				}
				Logger.debug(this, "Fetch successful");

				PropsFile messageProps = PropsFile.createPropsFile(result, true);
				String messageType = messageProps.get("messagetype");

				if(messageType == null) {
					Logger.error(this, "Got message without messagetype, discarding");
					slotManager.slotUsed();
					result.delete();
					continue;
				}

				if(messageType.equals("message")) {
					try {
						if(handleMessage(result, account.getMessageBank())) {
							slotManager.slotUsed();
						}
					} catch(ConnectionTerminatedException e) {
						Logger.debug(this, "Connection terminated");
						result.delete();
						return;
					}
				} else if(messageType.equals("cts")) {
					Logger.minor(this, "Successfully received CTS");

					boolean success;
					synchronized(channelProps) {
						success = channelProps.put("status", "cts-received");
					}

					if(success) {
						slotManager.slotUsed();
					}
				} else if(messageType.equals("ack")) {
					if(handleAck(result)) {
						slotManager.slotUsed();
					}
				} else {
					Logger.error(this, "Got message of unknown type: " + messageType);
					slotManager.slotUsed();
				}

				if(!result.delete()) {
					Logger.error(this, "Deletion of " + result + " failed");
				}
			}

			//Reschedule
			executor.schedule(fetcher, 5, TimeUnit.MINUTES);
		}

		public void execute() {
			Logger.debug(this, "Scheduling Fetcher for execution");
			executor.execute(fetcher);
		}
	}

	private class Sender implements Runnable {
		@Override
		public synchronized void run() {
			Logger.debug(this, "Sender running");

			try {
				realRun();
			} catch(RuntimeException e) {
				Logger.debug(this, "Caugth " + e);
				e.printStackTrace();
				throw e;
			} catch(Error e) {
				Logger.debug(this, "Caugth " + e);
				e.printStackTrace();
				throw e;
			}
		}

		private void realRun() {
			//Get a message from the outbox
			File outbox = new File(channelDir, OUTBOX_DIR_NAME);
			if(!outbox.exists()) {
				Logger.debug(this, "Outbox directory doesn't exist, so no messages to send");
				return;
			}

			File[] messages = outbox.listFiles();
			if(messages == null) {
				Logger.error(this, "Couldn't list messages in the outbox, trying again later");
				executor.schedule(sender, 5, TimeUnit.MINUTES);
				return;
			}

			//Pick the first message
			File message = null;
			for(File msg : messages) {
				if(msg.isFile()) {
					message = msg;
					Logger.debug(this, "Sending " + message);
					break;
				}

				Logger.debug(this, "Unexpected file in outbox: " + msg);
			}
			if(message == null) {
				Logger.debug(this, "Didn't find any messages to send");
				return;
			}

			String baseKey;
			synchronized(channelProps) {
				baseKey = channelProps.get(PropsKeys.PRIVATE_KEY);
			}
			if(baseKey == null) {
				Logger.debug(this, "Can't insert, missing private key");
				return;
			}

			String sendCode;
			synchronized(channelProps) {
				sendCode = channelProps.get(PropsKeys.SEND_CODE);
			}
			if(sendCode == null) {
				Logger.error(this, "Contact " + channelDir.getName() + " is corrupt - account file has no '" + PropsKeys.SEND_CODE + "' entry!");
				//TODO: Either delete the channel or resend the RTS
				return;
			}
			baseKey += sendCode + "-";

			InputStream data;
			try {
				//TODO: Read past the header
				data = new FileInputStream(message);
			} catch(FileNotFoundException e1) {
				Logger.debug(this, "Message file deleted after listing files, trying again later");
				executor.schedule(sender, 5, TimeUnit.MINUTES);
				return;
			}

			while(true) {
				String slot;
				synchronized(channelProps) {
					slot = channelProps.get(PropsKeys.SEND_SLOT);
				}

				Logger.debug(this, "Inserting data to " + baseKey + slot);
				FCPInsertErrorMessage fcpMessage;
				try {
					fcpMessage = fcpClient.put(data, baseKey + slot);
				} catch(FCPBadFileException e) {
					Logger.debug(this, "Caugth " + e);
					return;
				} catch(ConnectionTerminatedException e) {
					Logger.debug(this, "Caugth " + e);
					return;
				}

				if(fcpMessage == null) {
					slot = calculateNextSlot(slot);
					synchronized(channelProps) {
						channelProps.put(PropsKeys.SEND_SLOT, slot);
					}

					break;
				}

				if(fcpMessage.errorcode == FCPInsertErrorMessage.COLLISION) {
					slot = calculateNextSlot(slot);

					//Write the new slot each round so we won't have
					//to check all of them again if we fail
					synchronized(channelProps) {
						channelProps.put(PropsKeys.SEND_SLOT, slot);
					}

					Logger.debug(this, "Insert collided, trying slot " + slot);
					continue;
				}

				Logger.debug(this, "Insert to slot " + slot + " failed: " + fcpMessage);
				executor.schedule(sender, 5, TimeUnit.MINUTES);
				return;
			}

			message.delete();

			//Check for more messages
			executor.execute(sender);
			return;
		}

		public void execute() {
			Logger.debug(this, "Scheduling Sender for execution");
			executor.execute(sender);
		}
	}

	private class RTSSender implements Runnable {
		@Override
		public synchronized void run() {
			Logger.debug(this, "RTSSender running");

			try {
				realRun();
			} catch(RuntimeException e) {
				Logger.debug(this, "Caugth " + e);
				e.printStackTrace();
				throw e;
			} catch(Error e) {
				Logger.debug(this, "Caugth " + e);
				e.printStackTrace();
				throw e;
			}
		}

		private void realRun() {
			//Check when the RTS should be sent
			long sendRtsIn = sendRTSIn();
			if(sendRtsIn < 0) {
				return;
			}
			if(sendRtsIn > 0) {
				Logger.debug(this, "Rescheduling RTSSender in " + sendRtsIn + " ms when the RTS is due to be inserted");
				executor.schedule(this, sendRtsIn, TimeUnit.MILLISECONDS);
				return;
			}

			//Get mailsite key from WoT
			WoTConnection wotConnection = freemail.getWotConnection();
			if(wotConnection == null) {
				//WoT isn't loaded, so try again later
				Logger.debug(this, "WoT not loaded, trying again in 5 minutes");
				executor.schedule(this, 5, TimeUnit.MINUTES);
				return;
			}

			//FIXME: Get the truster id in a better way
			String senderId = channelDir.getParentFile().getParentFile().getName();
			Logger.debug(this, "Getting identity from WoT");
			Identity recipient = wotConnection.getIdentity(channelDir.getName(), senderId);
			if(recipient == null) {
				Logger.debug(this, "Didn't get identity from WoT, trying again in 5 minutes");
				executor.schedule(this, 5, TimeUnit.MINUTES);
				return;
			}

			//Strip the WoT part from the key and add the Freemail path
			String mailsiteKey = recipient.getRequestURI();
			mailsiteKey = mailsiteKey.substring(0, mailsiteKey.indexOf("/"));
			mailsiteKey = mailsiteKey + "/mailsite/0/mailpage";

			//Fetch the mailsite
			File mailsite;
			try {
				Logger.debug(this, "Fetching mailsite from " + mailsiteKey);
				mailsite = fcpClient.fetch(mailsiteKey);
			} catch(ConnectionTerminatedException e) {
				Logger.debug(this, "FCP connection has been terminated");
				return;
			} catch(FCPFetchException e) {
				Logger.debug(this, "Mailsite fetch failed (" + e + "), trying again in 5 minutes");
				executor.schedule(this, 5, TimeUnit.MINUTES);
				return;
			}

			//Get RTS KSK
			PropsFile mailsiteProps = PropsFile.createPropsFile(mailsite, false);
			String rtsKey = mailsiteProps.get("rtsksk");

			//Get or generate RTS values
			String privateKey;
			String publicKey;
			String initiatorSlot;
			String responderSlot;
			synchronized(channelProps) {
				privateKey = channelProps.get(PropsKeys.PRIVATE_KEY);
				publicKey = channelProps.get(PropsKeys.PUBLIC_KEY);
				initiatorSlot = channelProps.get(PropsKeys.SEND_SLOT);
				responderSlot = channelProps.get(PropsKeys.FETCH_SLOT);

				if((privateKey == null) || (publicKey == null)) {
					SSKKeyPair keyPair;
					try {
						Logger.debug(this, "Making new key pair");
						keyPair = fcpClient.makeSSK();
					} catch(ConnectionTerminatedException e) {
						Logger.debug(this, "FCP connection has been terminated");
						return;
					}
					privateKey = keyPair.privkey;
					publicKey = keyPair.pubkey;
				}
				if(initiatorSlot == null) {
					initiatorSlot = generateRandomSlot();
				}
				if(responderSlot == null) {
					responderSlot = generateRandomSlot();
				}

				channelProps.put(PropsKeys.PUBLIC_KEY, publicKey);
				channelProps.put(PropsKeys.PRIVATE_KEY, privateKey);
				channelProps.put(PropsKeys.SEND_SLOT, initiatorSlot);
				channelProps.put(PropsKeys.FETCH_SLOT, responderSlot);
				channelProps.put(PropsKeys.SEND_CODE, "i");
				channelProps.put(PropsKeys.FETCH_CODE, "r");
			}

			//Get the senders mailsite key
			Logger.debug(this, "Getting sender identity from WoT");
			Identity senderIdentity = wotConnection.getIdentity(senderId, senderId);
			if(senderIdentity == null) {
				Logger.debug(this, "Didn't get identity from WoT, trying again in 5 minutes");
				executor.schedule(this, 5, TimeUnit.MINUTES);
				return;
			}
			String senderMailsiteKey = recipient.getRequestURI();
			senderMailsiteKey = senderMailsiteKey.substring(0, senderMailsiteKey.indexOf("/"));
			senderMailsiteKey = senderMailsiteKey + "/mailsite/0/mailpage";

			//Now build the RTS
			byte[] rtsMessageBytes = buildRTSMessage(senderMailsiteKey, recipient.getIdentityID(), privateKey, initiatorSlot, responderSlot);
			if(rtsMessageBytes == null) {
				return;
			}

			//Sign the message
			byte[] signedMessage = signRtsMessage(rtsMessageBytes);
			if(signedMessage == null) {
				return;
			}

			//Encrypt the message using the recipients public key
			String keyModulus = mailsiteProps.get("asymkey.modulus");
			if(keyModulus == null) {
				Logger.error(this, "Mailsite is missing public key modulus");
				executor.schedule(this, 1, TimeUnit.HOURS);
				return;
			}

			String keyExponent = mailsiteProps.get("asymkey.pubexponent");
			if(keyExponent == null) {
				Logger.error(this, "Mailsite is missing public key exponent");
				executor.schedule(this, 1, TimeUnit.HOURS);
				return;
			}

			byte[] rtsMessage = encryptMessage(signedMessage, keyModulus, keyExponent);

			//Insert
			int slot;
			try {
				String key = "KSK@" + rtsKey + "-" + DateStringFactory.getKeyString();
				Logger.debug(this, "Inserting RTS to " + key);
				slot = fcpClient.slotInsert(rtsMessage, key, 1, "");
			} catch(ConnectionTerminatedException e) {
				return;
			}
			if(slot < 0) {
				Logger.debug(this, "Slot insert failed, trying again in 5 minutes");
				executor.schedule(this, 5, TimeUnit.MINUTES);
				return;
			}

			//Update channel props file
			synchronized(channelProps) {
				//Check if we've gotten the CTS while inserting the RTS
				if(!"cts-received".equals(channelProps.get(PropsKeys.SENDER_STATE))) {
					channelProps.put(PropsKeys.SENDER_STATE, "rts-sent");
				}
				channelProps.put(PropsKeys.RTS_SENT_AT, Long.toString(System.currentTimeMillis()));
			}

			long delay = sendRTSIn();
			if(delay < 0) {
				return;
			}
			Logger.debug(this, "Rescheduling RTSSender to run in " + delay + " ms when the reinsert is due");
			executor.schedule(this, delay, TimeUnit.MILLISECONDS);
		}

		public void execute() {
			Logger.debug(this, "Scheduling RTSSender for execution");
			executor.execute(this);
		}

		/**
		 * Returns the number of milliseconds left to when the RTS should be sent, or -1 if it
		 * should not be sent.
		 * @return the number of milliseconds left to when the RTS should be sent
		 */
		private long sendRTSIn() {
			//Check if the CTS has been received
			String channelState;
			synchronized(channelProps) {
				channelState = channelProps.get(PropsKeys.SENDER_STATE);
			}
			if((channelState != null) && channelState.equals("cts-received")) {
				Logger.debug(this, "CTS has been received");
				return -1;
			}

			//Check when the RTS should be (re)sent
			String rtsSentAt;
			synchronized(channelProps) {
				rtsSentAt = channelProps.get(PropsKeys.RTS_SENT_AT);
			}

			if(rtsSentAt != null) {
				long sendTime;
				try {
					sendTime = Long.parseLong(rtsSentAt);
				} catch(NumberFormatException e) {
					Logger.debug(this, "Illegal value in " + PropsKeys.RTS_SENT_AT + " field, assuming 0");
					sendTime = 0;
				}

				long timeToResend = (24 * 60 * 60 * 1000) - (System.currentTimeMillis() - sendTime);
				if(timeToResend > 0) {
					return timeToResend;
				}
			}

			//Send the RTS immediately
			return 0;
		}

		private byte[] buildRTSMessage(String senderMailsiteKey, String recipientIdentityID, String channelPrivateKey, String initiatorSlot, String responderSlot) {
			StringBuffer rtsMessage = new StringBuffer();
			rtsMessage.append("mailsite=" + senderMailsiteKey + "\r\n");
			rtsMessage.append("to=" + recipientIdentityID + "\r\n");
			rtsMessage.append("channel=" + channelPrivateKey + "\r\n");
			rtsMessage.append("initiatorSlot=" + initiatorSlot + "\r\n");
			rtsMessage.append("responderSlot=" + responderSlot + "\r\n");
			rtsMessage.append("\r\n");

			byte[] rtsMessageBytes;
			try {
				rtsMessageBytes = rtsMessage.toString().getBytes("UTF-8");
			} catch(UnsupportedEncodingException e) {
				Logger.error(this, "JVM doesn't support UTF-8 charset");
				e.printStackTrace();
				return null;
			}

			return rtsMessageBytes;
		}

		private byte[] signRtsMessage(byte[] rtsMessageBytes) {
			SHA256Digest sha256 = new SHA256Digest();
			sha256.update(rtsMessageBytes, 0, rtsMessageBytes.length);
			byte[] hash = new byte[sha256.getDigestSize()];
			sha256.doFinal(hash, 0);

			RSAKeyParameters ourPrivateKey = AccountManager.getPrivateKey(account.getProps());

			AsymmetricBlockCipher signatureCipher = new RSAEngine();
			signatureCipher.init(true, ourPrivateKey);
			byte[] signature = null;
			try {
				signature = signatureCipher.processBlock(hash, 0, hash.length);
			} catch(InvalidCipherTextException e) {
				Logger.error(this, "Failed to RSA encrypt hash: " + e.getMessage());
				e.printStackTrace();
				return null;
			}

			byte[] signedMessage = new byte[rtsMessageBytes.length + signature.length];
			System.arraycopy(rtsMessageBytes, 0, signedMessage, 0, rtsMessageBytes.length);
			System.arraycopy(signature, 0, signedMessage, rtsMessageBytes.length, signature.length);

			return signedMessage;
		}

		private byte[] encryptMessage(byte[] signedMessage, String keyModulus, String keyExponent) {
			//Make a new symmetric key for the message
			byte[] aesKeyAndIV = new byte[32 + 16];
			SecureRandom rnd = new SecureRandom();
			rnd.nextBytes(aesKeyAndIV);

			//Encrypt the message with the new symmetric key
			PaddedBufferedBlockCipher aesCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
			KeyParameter aesKeyParameters = new KeyParameter(aesKeyAndIV, 16, 32);
			ParametersWithIV aesParameters = new ParametersWithIV(aesKeyParameters, aesKeyAndIV, 0, 16);
			aesCipher.init(true, aesParameters);

			byte[] encryptedMessage = new byte[aesCipher.getOutputSize(signedMessage.length)];
			int offset = aesCipher.processBytes(signedMessage, 0, signedMessage.length, encryptedMessage, 0);

			try {
				aesCipher.doFinal(encryptedMessage, offset);
			} catch(InvalidCipherTextException e) {
				Logger.error(this, "Failed to perform symmertic encryption on RTS data: " + e.getMessage());
				e.printStackTrace();
				return null;
			}

			RSAKeyParameters recipientPublicKey = new RSAKeyParameters(false, new BigInteger(keyModulus, 32), new BigInteger(keyExponent, 32));
			AsymmetricBlockCipher keyCipher = new RSAEngine();
			keyCipher.init(true, recipientPublicKey);
			byte[] encryptedAesParameters = null;
			try {
				encryptedAesParameters = keyCipher.processBlock(aesKeyAndIV, 0, aesKeyAndIV.length);
			} catch(InvalidCipherTextException e) {
				Logger.error(this, "Failed to perform asymmertic encryption on RTS symmetric key: " + e.getMessage());
				e.printStackTrace();
				return null;
			}

			//Assemble the final message
			byte[] rtsMessage = new byte[encryptedAesParameters.length + encryptedMessage.length];
			System.arraycopy(encryptedAesParameters, 0, rtsMessage, 0, encryptedAesParameters.length);
			System.arraycopy(encryptedMessage, 0, rtsMessage, encryptedAesParameters.length, encryptedMessage.length);

			return rtsMessage;
		}
	}

	private boolean handleMessage(File msg, MessageBank mb) throws ConnectionTerminatedException {
		// parse the Freemail header(s) out.
		PropsFile msgprops = PropsFile.createPropsFile(msg, true);
		String s_id = msgprops.get("id");
		if (s_id == null) {
			Logger.error(this,"Message is missing id. Discarding.");
			msgprops.closeReader();
			return true;
		}

		int id;
		try {
			id = Integer.parseInt(s_id);
		} catch (NumberFormatException nfe) {
			Logger.error(this,"Got a message with an invalid (non-integer) id. Discarding.");
			msgprops.closeReader();
			return true;
		}

		MessageLog msglog = new MessageLog(this.channelDir);
		boolean isDupe;
		try {
			isDupe = msglog.isPresent(id);
		} catch (IOException ioe) {
			Logger.error(this,"Couldn't read logfile, so don't know whether received message is a duplicate or not. Leaving in the queue to try later.");
			msgprops.closeReader();
			return false;
		}
		if (isDupe) {
			Logger.normal(this,"Got a message, but we've already logged that message ID as received. Discarding.");
			msgprops.closeReader();
			return true;
		}

		BufferedReader br = msgprops.getReader();
		if (br == null) {
			Logger.error(this,"Got an invalid message. Discarding.");
			msgprops.closeReader();
			return true;
		}

		try {
			this.storeMessage(br, mb);
		} catch (IOException ioe) {
			return false;
		}
		Logger.normal(this,"You've got mail!");
		try {
			msglog.add(id);
		} catch (IOException ioe) {
			// how should we handle this? Remove the message from the inbox again?
			Logger.error(this,"warning: failed to write log file!");
		}

		queueAck(id);

		return true;
	}

	//TODO: This is mostly duplicated from sendMessage
	private void queueAck(int ackId) {
		long messageId;
		synchronized(channelProps) {
			messageId = Long.parseLong(channelProps.get(PropsKeys.MESSAGE_ID));
			channelProps.put(PropsKeys.MESSAGE_ID, messageId + 1);
		}

		//Write the message and attributes to the outbox
		File outbox = new File(channelDir, OUTBOX_DIR_NAME);
		if(!outbox.exists()) {
			if(!outbox.mkdir()) {
				Logger.error(this, "Couldn't create outbox directory: " + outbox);
				return;
			}
		}

		File messageFile = new File(outbox, "" + messageId);
		if(messageFile.exists()) {
			//TODO: Pick next message id?
			Logger.error(this, "Message id already in use");
			return;
		}

		try {
			if(!messageFile.createNewFile()) {
				Logger.error(this, "Couldn't create message file: " + messageFile);
				return;
			}

			synchronized(messageIndex) {
				messageIndex.put(messageId + ".status", "unsent");
			}

			OutputStream os = new FileOutputStream(messageFile);
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

			//Then what will be the header of the inserted message
			pw.print("messagetype=ack\r\n");
			pw.print("id=" + ackId + "\r\n");
			pw.print("\r\n");
			pw.flush();
		} catch(IOException e) {
			if(messageFile.exists()) {
				if(!messageFile.delete()) {
					Logger.error(this, "Couldn't delete message file (" + messageFile + ") after IOException");
				}
			}

			return;
		}

		sender.execute();

		return;
	}

	private boolean handleAck(File result) {
		PropsFile ackProps = PropsFile.createPropsFile(result);
		String id = ackProps.get("id");

		File outbox = new File(channelDir, OUTBOX_DIR_NAME);
		if(!outbox.exists()) {
			Logger.minor(this, "Got ack for message " + id + ", but the outbox doesn't exist");
			return true;
		}

		File message = new File(outbox, "message-" + id);
		if(!message.exists()) {
			Logger.minor(this, "Got ack for message " + id + ", but the message doesn't exist");
			return true;
		}

		if(!message.delete()) {
			Logger.error(this, "Couldn't delete " + message);
			return false;
		}

		return true;
	}

	private List<QueuedMessage> getSendQueue() {
		File outbox = new File(channelDir, OUTBOX_DIR_NAME);
		File[] files = outbox.listFiles();
		List<QueuedMessage> messages = new LinkedList<QueuedMessage>();

		for(File file : files) {
			if(file.getName().equals(INDEX_NAME)) {
				continue;
			}

			int uid;
			try {
				uid = Integer.parseInt(file.getName());
			} catch (NumberFormatException nfe) {
				// how did that get there? just delete it
				Logger.normal(this,"Found spurious file in send queue: '"+file.getName()+"' - deleting.");
				file.delete();
				continue;
			}

			messages.add(new QueuedMessage(uid));
		}

		return messages;
	}

	private class QueuedMessage {
		private final int uid;
		private long addedTime;
		private long firstSendTime;
		private long lastSendTime;
		private final File file;

		private QueuedMessage(int uid) {
			this.uid = uid;

			File outbox = new File(channelDir, OUTBOX_DIR_NAME);
			file = new File(outbox, Integer.toString(uid));

			String first;
			String last;
			String added;
			synchronized(messageIndex) {
				first = messageIndex.get(uid+".firstSendTime");
				last = messageIndex.get(uid+".lastSendTime");
				added = messageIndex.get(uid+".addedTime");
			}

			if(first == null) {
				firstSendTime = -1;
			} else {
				firstSendTime = Long.parseLong(first);
			}

			if(last == null) {
				this.lastSendTime = -1;
			} else {
				this.lastSendTime = Long.parseLong(last);
			}

			if(added == null) {
				this.addedTime = System.currentTimeMillis();
			} else {
				this.addedTime = Long.parseLong(added);
			}
		}

		public boolean setMessageFile(File newfile) {
			return newfile.renameTo(this.file);
		}

		public boolean saveProps() {
			boolean suc = true;
			synchronized(messageIndex) {
				suc &= messageIndex.put(uid+".first_send_time", this.firstSendTime);
				suc &= messageIndex.put(uid+".last_send_time", this.lastSendTime);
				suc &= messageIndex.put(uid+".added_time", this.addedTime);
			}

			return suc;
		}

		public boolean delete() {
			synchronized(messageIndex) {
				messageIndex.remove(this.uid+".slot");
				messageIndex.remove(this.uid+".first_send_time");
				messageIndex.remove(this.uid+".last_send_time");
			}

			return this.file.delete();
		}
	}

	private class HashSlotManager extends SlotManager {
		HashSlotManager(SlotSaveCallback cb, Object userdata, String slotlist) {
			super(cb, userdata, slotlist);
		}

		@Override
		protected String incSlot(String slot) {
			return calculateNextSlot(slot);
		}
	}

	private static class MessageLog {
		private static final String LOGFILE = "log";
		private final File logfile;

		public MessageLog(File ibctdir) {
			this.logfile = new File(ibctdir, LOGFILE);
		}

		public boolean isPresent(int targetid) throws IOException {
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(this.logfile));
			} catch (FileNotFoundException fnfe) {
				return false;
			}

			String line;
			while ( (line = br.readLine()) != null) {
				int curid = Integer.parseInt(line);
				if (curid == targetid) {
					br.close();
					return true;
				}
			}

			br.close();
			return false;
		}

		public void add(int id) throws IOException {
			FileOutputStream fos = new FileOutputStream(this.logfile, true);

			PrintStream ps = new PrintStream(fos);
			ps.println(id);
			ps.close();
		}
	}

	private static class ChannelSlotSaveImpl implements SlotSaveCallback {
		private final PropsFile propsFile;
		private final String keyName;

		private ChannelSlotSaveImpl(PropsFile propsFile, String keyName) {
			this.propsFile = propsFile;
			this.keyName = keyName;
		}

		@Override
		public void saveSlots(String slots, Object userdata) {
			synchronized(propsFile) {
				propsFile.put(keyName, slots);
			}
		}
	}
}
