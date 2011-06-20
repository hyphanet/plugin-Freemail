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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.archive.util.Base32;
import org.bouncycastle.crypto.digests.SHA256Digest;

import freemail.AckProcrastinator;
import freemail.MessageBank;
import freemail.Postman;
import freemail.SlotManager;
import freemail.SlotSaveCallback;
import freemail.fcp.ConnectionTerminatedException;
import freemail.fcp.FCPBadFileException;
import freemail.fcp.FCPFetchException;
import freemail.fcp.FCPInsertErrorMessage;
import freemail.fcp.HighLevelFCPClient;
import freemail.utils.Logger;
import freemail.utils.PropsFile;
import freenet.support.SimpleFieldSet;

//FIXME: The message id gives away how many messages has been sent over the channel.
//       Could it be replaced by a different solution that gives away less information?
public class Channel extends Postman {
	private static final String CHANNEL_PROPS_NAME = "props";
	private static final int POLL_AHEAD = 6;
	private static final String OUTBOX_DIR_NAME = "outbox";

	//The keys used in the props file
	private static class PropsKeys {
		private static final String PRIVATE_KEY = "privateKey";
		private static final String PUBLIC_KEY = "publicKey";
		private static final String FETCH_SLOT = "fetchSlot";
		private static final String SEND_SLOT = "sendSlot";
		private static final String IS_INITIATOR = "isInitiator";
		private static final String MESSAGE_ID = "messageId";
	}

	private final File channelDir;
	private final PropsFile channelProps;
	private final Set<Observer> observers = new HashSet<Observer>();
	private final ScheduledExecutorService executor;

	private Fetcher fetcher;
	private final Object fetcherLock = new Object();

	public Channel(File channelDir, ScheduledExecutorService executor) {
		if(executor == null) throw new NullPointerException();
		this.executor = executor;

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
	}

	public void startTasks() {
		startFetcher();
	}

	private void startFetcher() {
		//Start fetcher if possible
		String fetchSlot;
		String isInitiator;
		String publicKey;
		synchronized(channelProps) {
			fetchSlot = channelProps.get(PropsKeys.FETCH_SLOT);
			isInitiator = channelProps.get(PropsKeys.IS_INITIATOR);
			publicKey = channelProps.get(PropsKeys.PUBLIC_KEY);
		}

		if((fetchSlot != null) && (isInitiator != null) && (publicKey != null)) {
			Fetcher f;
			synchronized(fetcherLock) {
				fetcher = new Fetcher();
				f = fetcher;
			}
			executor.execute(f);
		}
	}

	/**
	 * Places the data read from {@code message} on the send queue
	 * @param message the data to be sent
	 * @return {@code true} if the message was placed on the queue
	 * @throws NullPointerException if {@code message} is {@code null}
	 */
	public boolean sendMessage(InputStream message) {
		if(message == null) throw new NullPointerException("Parameter message was null");

		long messageId = Long.parseLong(channelProps.get(PropsKeys.MESSAGE_ID));
		channelProps.put(PropsKeys.MESSAGE_ID, messageId + 1);

		//Write the message and attributes to the outbox
		File outbox = new File(channelDir, OUTBOX_DIR_NAME);
		if(!outbox.exists()) {
			if(!outbox.mkdir()) {
				Logger.error(this, "Couldn't create outbox directory: " + outbox);
				return false;
			}
		}

		File messageFile = new File(outbox, "message-" + messageId);
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

			OutputStream os = new FileOutputStream(messageFile);
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

			//Write the header that will be sent later
			pw.print("messagetype=message\r\n");
			pw.print("messageid=" + messageId + "\r\n");
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

		return true;
	}

	/**
	 * Sends a message to the remote side of the channel containing the given headers and the data
	 * read from {@code message}. "messagetype" must be set in {@code header}. If message is
	 * {@code null} no data will be sent after the header.
	 * @param fcpClient the HighLevelFCPClient used to send the message
	 * @param header the headers to prepend to the message
	 * @param message the data to be sent
	 * @return {@code true} if the message was sent successfully
	 * @throws ConnectionTerminatedException if the FCP connection is terminated while sending
	 * @throws IOException if the InputStream throws an IOException
	 */
	boolean sendMessage(HighLevelFCPClient fcpClient, SimpleFieldSet header, InputStream message) throws ConnectionTerminatedException, IOException {
		assert (fcpClient != null);
		assert (header.get("messagetype") != null);

		String baseKey = channelProps.get(PropsKeys.PRIVATE_KEY);

		//SimpleFieldSet seems to only output using \n,
		//but we need \n\r so we need to do it manually
		StringBuilder headerString = new StringBuilder();
		Iterator<String> headerKeyIterator = header.keyIterator();
		while(headerKeyIterator.hasNext()) {
			String key = headerKeyIterator.next();
			String value = header.get(key);

			headerString.append(key + "=" + value + "\r\n");
		}
		headerString.append("\r\n");

		ByteArrayInputStream headerBytes = new ByteArrayInputStream(headerString.toString().getBytes("UTF-8"));
		InputStream data = (message == null) ? headerBytes : new SequenceInputStream(headerBytes, message);
		while(true) {
			String slot = channelProps.get(PropsKeys.SEND_SLOT);

			FCPInsertErrorMessage fcpMessage;
			try {
				fcpMessage = fcpClient.put(data, baseKey + slot);
			} catch(FCPBadFileException e) {
				throw new IOException(e);
			}

			if(fcpMessage == null) {
				slot = calculateNextSlot(slot);
				channelProps.put(PropsKeys.SEND_SLOT, slot);
				return true;
			}

			if(fcpMessage.errorcode == FCPInsertErrorMessage.COLLISION) {
				slot = calculateNextSlot(slot);

				//Write the new slot each round so we won't have
				//to check all of them again if we fail
				channelProps.put(PropsKeys.SEND_SLOT, slot);

				Logger.debug(this, "Insert collided, trying slot " + slot);
				continue;
			}

			Logger.debug(this, "Insert to slot " + slot + " failed: " + fcpMessage);
			return false;
		}
	}

	private String calculateNextSlot(String slot) {
		byte[] buf = Base32.decode(slot);
		SHA256Digest sha256 = new SHA256Digest();
		sha256.update(buf, 0, buf.length);
		sha256.doFinal(buf, 0);

		return Base32.encode(buf);
	}

	private class Fetcher implements Runnable {
		@Override
		public void run() {
			//TODO: Try fetching messages

			//Reschedule
			executor.schedule(this, 5, TimeUnit.MINUTES);
		}
	}

	/**
	 * Adds an observer to this Channel.
	 * @param observer the observer that should be added
	 * @throws NullPointerException if {@code observer} is {@code null}
	 */
	public void addObserver(Observer observer) {
		if(observer == null) throw new NullPointerException();

		synchronized(observers) {
			observers.add(observer);
		}
	}

	/**
	 * Removes an observer from this Channel.
	 * @param observer the observer that should be removed
	 */
	public void removeObserver(Observer observer) {
		//This is a bug in the caller, but leave it as an assert since it won't corrupt any state
		assert (observer != null);

		synchronized(observers) {
			observers.remove(observer);
		}
	}

	public interface Observer {
		public void fetched(InputStream data);
	}

	public void fetch(MessageBank mb, HighLevelFCPClient fcpcli) {
		String slots = this.channelProps.get(PropsKeys.FETCH_SLOT);
		if (slots == null) {
			Logger.error(this,"Contact "+this.channelDir.getName()+" is corrupt - account file has no '" + PropsKeys.FETCH_SLOT + "' entry!");
			// TODO: probably delete the contact. it's useless now.
			return;
		}

		HashSlotManager sm = new HashSlotManager(new ChannelSlotSaveImpl(channelProps, PropsKeys.FETCH_SLOT), null, slots);
		sm.setPollAhead(POLL_AHEAD);

		String basekey = this.channelProps.get(PropsKeys.PRIVATE_KEY);
		if (basekey == null) {
			Logger.error(this,"Contact "+this.channelDir.getName()+" is corrupt - account file has no '" + PropsKeys.PRIVATE_KEY + "' entry!");
			// TODO: probably delete the contact. it's useless now.
			return;
		}
		String slot;
		while ( (slot = sm.getNextSlot()) != null) {
			// the slot should be 52 characters long, since this is how long a 256 bit string ends up when base32 encoded.
			// (the slots being base32 encoded SHA-256 checksums)
			// TODO: remove this once the bug is ancient history, or if actually want to check the slots, do so in the SlotManagers.
			// a fix for the bug causing this (https://bugs.freenetproject.org/view.php?id=1087) was committed on Feb 4 2007,
			// anybody who has started using Freemail after that date is not affected.
			if(slot.length()!=52) {
				Logger.normal(this,"Ignoring malformed slot "+slot+" (probably due to previous bug). Please the fix the entry in "+this.channelDir);
				break;
			}
			String key = basekey+slot;

			Logger.minor(this,"Attempting to fetch mail on key "+key);
			File msg = null;
			try {
				msg = fcpcli.fetch(key);
			} catch (ConnectionTerminatedException cte) {
				return;
			} catch (FCPFetchException fe) {
				if(fe.isFatal()) {
					Logger.normal(this, "Fatal fetch failure, marking slot as used");
					sm.slotUsed();
				}

				Logger.minor(this,"No mail in slot (fetch returned "+fe.getMessage()+")");
				continue;
			}
			Logger.normal(this,"Found a message!");

			try {
				handleMessage(sm, msg, mb);
			} catch (ConnectionTerminatedException cte) {
				// terminated before we could validate the sender. Give up, and we won't mark the slot used so we'll
				// pick it up next time.
				return;
			}
		}
	}

	private void handleMessage(SlotManager sm, File msg, MessageBank mb) throws ConnectionTerminatedException {
		// parse the Freemail header(s) out.
		PropsFile msgprops = PropsFile.createPropsFile(msg, true);
		String s_id = msgprops.get("id");
		if (s_id == null) {
			Logger.error(this,"Got a message with an invalid header. Discarding.");
			sm.slotUsed();
			msgprops.closeReader();
			msg.delete();
			return;
		}

		int id;
		try {
			id = Integer.parseInt(s_id);
		} catch (NumberFormatException nfe) {
			Logger.error(this,"Got a message with an invalid (non-integer) id. Discarding.");
			sm.slotUsed();
			msgprops.closeReader();
			msg.delete();
			return;
		}

		MessageLog msglog = new MessageLog(this.channelDir);
		boolean isDupe;
		try {
			isDupe = msglog.isPresent(id);
		} catch (IOException ioe) {
			Logger.error(this,"Couldn't read logfile, so don't know whether received message is a duplicate or not. Leaving in the queue to try later.");
			msgprops.closeReader();
			msg.delete();
			return;
		}
		if (isDupe) {
			Logger.normal(this,"Got a message, but we've already logged that message ID as received. Discarding.");
			sm.slotUsed();
			msgprops.closeReader();
			msg.delete();
			return;
		}

		BufferedReader br = msgprops.getReader();
		if (br == null) {
			Logger.error(this,"Got an invalid message. Discarding.");
			sm.slotUsed();
			msgprops.closeReader();
			msg.delete();
			return;
		}

		try {
			this.storeMessage(br, mb);
			msg.delete();
		} catch (IOException ioe) {
			msg.delete();
			return;
		}
		Logger.normal(this,"You've got mail!");
		sm.slotUsed();
		try {
			msglog.add(id);
		} catch (IOException ioe) {
			// how should we handle this? Remove the message from the inbox again?
			Logger.error(this,"warning: failed to write log file!");
		}
		String ack_key = this.channelProps.get("ackssk");
		if (ack_key == null) {
			Logger.error(this,"Warning! Can't send message acknowledgement - don't have an 'ackssk' entry! This message will eventually bounce, even though you've received it.");
			return;
		}
		ack_key += "ack-"+id;
		AckProcrastinator.put(ack_key);
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
			propsFile.put(keyName, slots);
		}
	}
}
