/*
 * MessageHandler.java
 * This file is part of Freemail
 * Copyright (C) 2011 Martin Nyhus
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

package org.freenetproject.freemail.transport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

import org.archive.util.Base32;
import org.freenetproject.freemail.Freemail;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.FreemailPlugin;
import org.freenetproject.freemail.MailHeaderFilter;
import org.freenetproject.freemail.MailMessage;
import org.freenetproject.freemail.Postman;
import org.freenetproject.freemail.FreemailPlugin.TaskType;
import org.freenetproject.freemail.fcp.HighLevelFCPClient;
import org.freenetproject.freemail.transport.Channel.ChannelEventCallback;
import org.freenetproject.freemail.utils.EmailAddress;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.utils.PropsFile;
import org.freenetproject.freemail.wot.Identity;

import freenet.support.Base64;
import freenet.support.IllegalBase64Exception;
import freenet.support.Timer;
import freenet.support.api.Bucket;
import freenet.support.io.Closer;
import freenet.support.io.FileBucket;

/**
 * MessageHandler is the high level interface to the part of Freemail that sends messages over
 * Freenet. The general contract of MessageHandler is that submitted messages are either
 * delivered successfully to the recipient, or, for messages that can not be delivered, a failure
 * notice is delivered to the inbox of the sender. Messages received over Freenet are delivered to
 * the inbox of the account that is specified during construction.
 */
public class MessageHandler {
	private static final String INDEX_NAME = "index";
	private static final long RESEND_TIME = 24 * 60 * 60 * 1000;
	private static final String MSG_LOG_NAME = "log";

	/**
	 * Holds the static portions of the keys used in the index file. The values that are stored per
	 * message must be appended to the message number.
	 */
	private static class IndexKeys {
		/** Comma separated list of recipient ids that this message should be sent to */
		private static final String RECIPIENT = ".recipient";
		/** The message number that should be used for the next message that is submitted */
		private static final String NEXT_MESSAGE_NUMBER = "nextMessageNumber";
		/** The time when the message was first sent */
		private static final String FIRST_SEND_TIME = ".firstSendTime";
		/** The time when the message was last sent */
		private static final String LAST_SEND_TIME = ".lastSendTime";
		/** The message number used by the Channel for this message */
		private static final String MSG_NUM = ".msgNum";
	}

	private final File outbox;
	/** Channels (may be more than one per correspondent). LOCKING: Channels lock should be taken 
	 * before the locks inside Channel, not after. I.e. Channel should not call back to us with 
	 * locks held. */
	private final List<Channel> channels = new LinkedList<Channel>();
	private final Freemail freemail;
	private final File channelDir;
	private final FreemailAccount freemailAccount;
	private final AtomicInteger nextChannelNum = new AtomicInteger();
	private final ConcurrentHashMap<String, Future<?>> tasks = new ConcurrentHashMap<String, Future<?>>();

	public MessageHandler(File outbox, Freemail freemail, File channelDir, FreemailAccount freemailAccount) {
		this.outbox = outbox;
		this.freemail = freemail;
		this.channelDir = channelDir;
		this.freemailAccount = freemailAccount;

		//Create and start all the channels
		if(!channelDir.exists()) {
			if(!channelDir.mkdir()) {
				Logger.error(this, "Couldn't create channel directory: " + channelDir);
			}
		}

		for(File f : channelDir.listFiles()) {
			if(!f.isDirectory()) {
				Logger.error(this, "Spurious file in channel directory: " + f);
				continue;
			}

			try {
				int num = Integer.parseInt(f.getName());
				if(num >= nextChannelNum.get()) {
					nextChannelNum.set(num + 1);
				}
			} catch(NumberFormatException e) {
				Logger.error(this, "Found directory with malformed name: " + f);
				continue;
			}

			Logger.debug(this, "Initializing channel from directory " + f);
			try {
				Channel channel = new Channel(f, FreemailPlugin.getExecutor(TaskType.UNSPECIFIED), new HighLevelFCPClient(), freemail, freemailAccount, null);
				channel.setCallback(new AckCallback(channel.getRemoteIdentity()));
				channels.add(channel);
			} catch(ChannelTimedOutException e) {
				Logger.debug(this, "Deleting timed out channel");
				if(!Channel.deleteChannel(f)) {
					Logger.error(this, "Failed to delete channel because there are files left in " + f);
				}
			} catch(IllegalStateException e) {
				//Channel is broken for some reason so get rid of it
				Logger.error(this, "Deleting broken channel");
				if(!Channel.deleteChannel(f)) {
					Logger.error(this, "Failed to delete channel because "
							+ "there are files left in "+ f);
				}
			}
		}
	}

	public void start() {
		if(outbox.isDirectory()) {
			for(File rcptOutbox : outbox.listFiles()) {
				if(!rcptOutbox.isDirectory()) {
					Logger.error(this, "Spurious file in outbox: " + rcptOutbox);
					continue;
				}

				for(File f : rcptOutbox.listFiles()) {
					if(!f.isFile()) {
						Logger.error(this, "Spurious file in contact outbox: " + f);
						continue;
					}

					String identifier = f.getName();

					//Skip log and index
					if(identifier.equals(INDEX_NAME) || identifier.equals(MSG_LOG_NAME)) {
						continue;
					}

					String rawMsgNum;
					PropsFile props = PropsFile.createPropsFile(new File(rcptOutbox, INDEX_NAME));
					synchronized (props) {
						rawMsgNum = props.get(identifier + IndexKeys.MSG_NUM);
					}

					try {
						long num = Long.parseLong(rawMsgNum);
						Logger.debug(this, "Scheduling SenderTask for " + num);
						ScheduledExecutorService senderExecutor = FreemailPlugin.getExecutor(TaskType.SENDER);
						tasks.put(Long.toString(num), senderExecutor.schedule(new SenderTask(rcptOutbox, num), 0, TimeUnit.NANOSECONDS));
					} catch(NumberFormatException e) {
						Logger.error(this, "Found file without valid message number: " + f);
						continue;
					}
				}
			}
		}

		//Start the channel tasks
		synchronized(channels) {
			for(Channel channel : channels) {
				channel.startTasks();
			}
		}
	}

	public boolean sendMessage(List<Identity> recipients, Bucket message) throws IOException {
		if(!outbox.exists()) {
			if(!outbox.mkdir()) {
				Logger.error(this, "Couldn't create outbox directory: " + outbox);
				return false;
			}
		}

		for(Identity recipient : recipients) {
			File rcptOutbox = new File(outbox, recipient.getBase32IdentityID());
			if(!rcptOutbox.exists()) {
				if(!rcptOutbox.mkdir()) {
					Logger.error(this, "Couldn't create recipient outbox directory: " + rcptOutbox);
					return false;
				}
			}

			long msgNum = getMessageNumber(rcptOutbox);
			String identifier = Long.toString(msgNum);
			File messageFile = new File(rcptOutbox, "" + identifier);

			if(!messageFile.createNewFile()) {
				Logger.error(this, "Message file " + messageFile + " already exists");
				return false;
			}

			OutputStream os = new FileOutputStream(messageFile);
			PrintWriter pw = new PrintWriter(os);
			InputStream messageStream = message.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(messageStream));
			MailHeaderFilter filter = new MailHeaderFilter(reader);
			try {
				//Copy headers
				String header = filter.readHeader();
				while(header != null) {
					pw.write(header + "\r\n");
					header = filter.readHeader();
				}
				pw.write("\r\n");

				//Then the rest of the message
				char[] buffer = new char[1024];
				while(true) {
					int read = reader.read(buffer, 0, buffer.length);
					if(read == -1) break;
					pw.write(buffer, 0, read);
				}
			} finally {
				Closer.close(pw);
				Closer.close(reader);
			}

			PropsFile props = PropsFile.createPropsFile(new File(rcptOutbox, INDEX_NAME));
			synchronized(props) {
				props.put(identifier + IndexKeys.RECIPIENT, recipient.getIdentityID());
				props.put(identifier + IndexKeys.MSG_NUM, Long.toString(msgNum));
			}

			ScheduledExecutorService senderExecutor = FreemailPlugin.getExecutor(TaskType.SENDER);
			tasks.put(identifier, senderExecutor.submit(new SenderTask(rcptOutbox, msgNum)));
		}

		return true;
	}

	private Channel getChannel(String remoteIdentity) {
		synchronized(channels) {
			for(Channel c : channels) {
				if(remoteIdentity.equals(c.getRemoteIdentity()) && c.canSendMessages()) {
					return c;
				}
			}

			//The channel didn't exist or it has timed out, so create a new one
			File newChannelDir = new File(channelDir, "" + nextChannelNum.getAndIncrement());
			if(!newChannelDir.mkdir()) {
				Logger.error(this, "Couldn't create the channel directory");
				return null;
			}

			Channel channel;
			try {
				channel = new Channel(newChannelDir, FreemailPlugin.getExecutor(TaskType.UNSPECIFIED), new HighLevelFCPClient(), freemail, freemailAccount, remoteIdentity);
				channel.setCallback(new AckCallback(remoteIdentity));
			} catch(ChannelTimedOutException e) {
				//Can't happen since we're creating a new channel
				throw new AssertionError("Caugth ChannelTimedOutException when creating a new channel");
			}
			channel.startTasks();
			channels.add(channel);

			return channel;
		}
	}

	public Channel createChannelFromRTS(PropsFile rtsProps) {
		//First try to find a channel with the same key
		String rtsPrivateKey = rtsProps.get("channel");
		synchronized(channels) {
			for(Channel c : channels) {
				if(rtsPrivateKey.equals(c.getPrivateKey())) {
					c.processRTS(rtsProps);
					return c;
				}
			}

			//Create a new channel from the RTS values
			Logger.debug(this, "Creating new channel from RTS");
			File newChannelDir = new File(channelDir, "" + nextChannelNum.getAndIncrement());
			if(!newChannelDir.mkdir()) {
				Logger.error(this, "Couldn't create the channel directory");
				return null;
			}

			String remoteIdentity = rtsProps.get("mailsite");
			remoteIdentity = remoteIdentity.substring(remoteIdentity.indexOf("@") + 1); //Strip USK@
			remoteIdentity = remoteIdentity.substring(0, remoteIdentity.indexOf(","));

			Channel channel;
			try {
				channel = new Channel(newChannelDir, FreemailPlugin.getExecutor(TaskType.UNSPECIFIED), new HighLevelFCPClient(), freemail, freemailAccount, remoteIdentity);
				channel.setCallback(new AckCallback(remoteIdentity));
			} catch(ChannelTimedOutException e) {
				//Can't happen since we're creating a new channel
				throw new AssertionError("Caugth ChannelTimedOutException when creating a new channel");
			}
			channel.processRTS(rtsProps);
			channel.startTasks();
			channels.add(channel);

			return channel;
		}
	}

	public List<OutboxMessage> listOutboxMessages() throws IOException {
		List<OutboxMessage> messages = new LinkedList<OutboxMessage>();

		File[] outboxFiles = outbox.listFiles();
		if(outboxFiles == null) {
			return messages;
		}

		for(File rcptOutbox : outboxFiles) {
			if(!rcptOutbox.isDirectory()) {
				continue;
			}

			PropsFile props = PropsFile.createPropsFile(new File(rcptOutbox, INDEX_NAME));
			for(File message : rcptOutbox.listFiles()) {
				String identifier = message.getName();

				String recipient;
				String firstSendTime;
				String lastSendTime;
				synchronized(props) {
					recipient = props.get(identifier + IndexKeys.RECIPIENT);
					if(recipient == null) {
						//Not a message
						continue;
					}

					firstSendTime = props.get(identifier + IndexKeys.FIRST_SEND_TIME);
					lastSendTime = props.get(identifier + IndexKeys.LAST_SEND_TIME);
				}

				OutboxMessage msg = new OutboxMessage(recipient, firstSendTime, lastSendTime, message);
				messages.add(msg);
			}
		}

		return messages;
	}

	public class OutboxMessage {
		public final String recipient;
		public final String subject;

		private final Date firstSendTime;
		private final Date lastSendTime;

		private OutboxMessage(String recipient, String firstSendTime, String lastSendTime, File message) throws IOException {
			this.recipient = recipient;

			Date first;
			try {
				first = new Date(Long.parseLong(firstSendTime));
			} catch(NumberFormatException e) {
				first = null;
			}
			this.firstSendTime = first;

			Date last;
			try {
				last = new Date(Long.parseLong(lastSendTime));
			} catch(NumberFormatException e) {
				last = null;
			}
			this.lastSendTime = last;

			MailMessage msg = new MailMessage(message, 0);
			msg.readHeaders();
			String s;
			try {
				s = msg.getSubject();
			} catch (UnsupportedEncodingException e) {
				s = msg.getFirstHeader("subject");
			}
			subject = s;
			Logger.debug(this, "Read subject: " + subject);
		}

		public Date getFirstSendTime() {
			if(firstSendTime == null) return null;
			return new Date(firstSendTime.getTime());
		}

		public Date getLastSendTime() {
			if(lastSendTime == null) return null;
			return new Date(lastSendTime.getTime());
		}
	}

	private long getMessageNumber(File rcptOutbox) {
		PropsFile props = PropsFile.createPropsFile(new File(rcptOutbox, INDEX_NAME));
		synchronized(props) {
			String rawNumber = props.get(IndexKeys.NEXT_MESSAGE_NUMBER);
			long number;
			try {
				number = Long.parseLong(rawNumber);
			} catch(NumberFormatException e) {
				number = 0;

				/* Ignore null since it will always be missing the first time */
				if(rawNumber != null) {
					Logger.error(this, "Parsing of next message number failed, was " + rawNumber);
				}
			}
			props.put(IndexKeys.NEXT_MESSAGE_NUMBER, "" + (number + 1));
			return number;
		}
	}

	private class SenderTask implements Runnable {
		private final long msgNum;
		private final String identifier;
		private final File rcptOutbox;

		private SenderTask(File rcptOutbox, long msgNum) {
			this.msgNum = msgNum;
			this.identifier = Long.toString(msgNum);
			this.rcptOutbox = rcptOutbox;
		}

		@Override
		public void run() {
			Logger.minor(this, "SenderTask for message " + identifier + " on account " + freemailAccount.getIdentity() + " running");

			long retryIn;
			long lastSendTime;
			PropsFile props = PropsFile.createPropsFile(new File(rcptOutbox, INDEX_NAME));
			try {
				String time;
				synchronized(props) {
					time = props.get(identifier + IndexKeys.LAST_SEND_TIME);
				}
				lastSendTime = Long.parseLong(time);
			} catch(NumberFormatException e) {
				lastSendTime = 0;
			}

			retryIn = (lastSendTime + RESEND_TIME) - System.currentTimeMillis();
			if(retryIn <= 0) {
				boolean inserted;
				try {
					Timer insertTimer = Timer.start();
					inserted = sendMessage();
					insertTimer.log(this, 1, TimeUnit.HOURS, "Total time spent sending message");
				} catch (InterruptedException e) {
					Logger.debug(this, "SenderTask interrupted, quitting");
					return;
				}
				if(!inserted) {
					//In most cases this is because the RTS hasn't been sent yet (so keys etc.
					//haven't been generated yet), or because the insert failed
					retryIn = 5 * 60 * 1000; //5 minutes
				} else {
					synchronized(props) {
						long curTime = System.currentTimeMillis();
						String firstSentTime = props.get(identifier + IndexKeys.FIRST_SEND_TIME);
						if(firstSentTime == null) {
							props.put(identifier + IndexKeys.FIRST_SEND_TIME, "" + curTime);
						}
						props.put(identifier + IndexKeys.LAST_SEND_TIME, "" + curTime);
					}

					retryIn = RESEND_TIME;
				}
			}

			//Schedule again when the resend is due
			Logger.minor(this, "Rescheduling sender task in " + retryIn + "ms");
			ScheduledExecutorService senderExecutor = FreemailPlugin.getExecutor(TaskType.SENDER);
			tasks.put(identifier, senderExecutor.schedule(this, retryIn, TimeUnit.MILLISECONDS));
		}

		private boolean sendMessage() throws InterruptedException {
			String recipient;
			PropsFile props = PropsFile.createPropsFile(new File(rcptOutbox, INDEX_NAME));
			synchronized(props) {
				recipient = props.get(identifier + IndexKeys.RECIPIENT);
			}

			Channel c;
			boolean inserted;
			while(true) {
				c = getChannel(recipient);
				Bucket message = new FileBucket(new File(rcptOutbox, identifier), false, false, false, false, false);
				try {
					inserted = c.sendMessage(message, msgNum);
				} catch(ChannelTimedOutException e) {
					//Try again with a new channel
					continue;
				} catch(IOException e) {
					Logger.error(this, "Caugth IOException while sending message: " + e.getMessage(), e);
					inserted = false;
				}

				break;
			}

			return inserted;
		}
	}

	private void deleteIndexEntries(File rcptOutbox, String identifier) {
		PropsFile props = PropsFile.createPropsFile(new File(rcptOutbox, INDEX_NAME));
		synchronized(props) {
			props.remove(identifier + IndexKeys.FIRST_SEND_TIME);
			props.remove(identifier + IndexKeys.LAST_SEND_TIME);
			props.remove(identifier + IndexKeys.RECIPIENT);
			props.remove(identifier + IndexKeys.MSG_NUM);
		}
	}

	private class AckCallback extends Postman implements ChannelEventCallback {
		private final String remoteId;
		private final MessageLog msgLog;

		private AckCallback(String remoteId) {
			assert (remoteId != null);
			try {
				this.remoteId = Base32.encode(Base64.decode(remoteId)).toLowerCase();
			} catch (IllegalBase64Exception e) {
				throw new AssertionError();
			}

			File rcptOutbox = new File(outbox, remoteId);
			if(!rcptOutbox.exists()) {
				if(!rcptOutbox.mkdirs()) {
					//Will throw IOException when the message log is used later
					Logger.error(this, "Couldn't create recipient outbox (" + rcptOutbox
							+ "), so can't handle incoming message");
				}
			}

			msgLog = new MessageLog(new File(rcptOutbox, MSG_LOG_NAME));
		}

		@Override
		public void onAckReceived(long id) {
			File rcptOutbox = new File(outbox, remoteId);

			File message = new File(rcptOutbox, "" + id);
			if(message.exists()) {
				Logger.minor(this, "Received ack, deleting message file: " + message);
				if(!message.delete()) {
					Logger.error(this, "Couldn't delete " + message);
				}
			} else {
				Logger.minor(this, "Received ack but message file doesn't exits: " + message);
			}

			deleteIndexEntries(rcptOutbox, Long.toString(id));

			Future<?> task = tasks.remove(Long.toString(id));
			if(task != null) {
				//Stop the insert if possible, but don't interrupt since the FCP code ignores it
				task.cancel(false);
			}
		}

		@Override
		public boolean handleMessage(Channel channel, BufferedReader message, long id) {
			synchronized (msgLog) {
				boolean isDupe;
				try {
					isDupe = msgLog.isPresent(id);
				} catch (IOException ioe) {
					Logger.error(this, "Couldn't read logfile, so don't know whether received message is a duplicate or not. Leaving in the queue to try later.", ioe);
					return false;
				}
				if(isDupe) {
					Logger.normal(this, "Got a message, but we've already logged that message ID as received. Discarding.");
					return true;
				}

				try {
					storeMessage(message, freemailAccount.getMessageBank());
				} catch(IOException e) {
					return false;
				}
				Logger.normal(this, "You've got mail!");
				try {
					msgLog.add(id, null);
				} catch(IOException e) {
					// how should we handle this? Remove the message from the inbox again?
					Logger.error(this, "Failed to write to the log file. This can lead to duplicate messages", e);
				}
			}

			return true;
		}

		@Override
		public boolean validateFrom(EmailAddress address) {
			if(remoteId.equalsIgnoreCase(address.getSubDomain())) {
				return true;
			}

			Logger.debug(this, "Marking as spoofed since subdomain doesn't match. Was: "
					+ address.getSubDomain() + " Expected: " + remoteId);
			return false;
		}
	}
}
