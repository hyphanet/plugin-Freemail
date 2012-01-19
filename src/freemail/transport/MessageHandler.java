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

package freemail.transport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

import org.archive.util.Base32;

import freemail.Freemail;
import freemail.FreemailAccount;
import freemail.FreemailPlugin;
import freemail.FreemailPlugin.TaskType;
import freemail.MailMessage;
import freemail.Postman;
import freemail.fcp.ConnectionTerminatedException;
import freemail.fcp.HighLevelFCPClient;
import freemail.transport.Channel.ChannelEventCallback;
import freemail.utils.Logger;
import freemail.utils.PropsFile;
import freemail.wot.Identity;
import freenet.support.Base64;
import freenet.support.IllegalBase64Exception;
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
	private final static String INDEX_NAME = "index";
	private final static long RESEND_TIME = 24 * 60 * 60 * 1000;
	private final static String MSG_LOG_NAME = "log";

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
	private final List<Channel> channels = new LinkedList<Channel>();
	private final Freemail freemail;
	private final File channelDir;
	private final FreemailAccount freemailAccount;
	private final AtomicInteger nextChannelNum = new AtomicInteger();
	private final AckCallback ackCallback = new AckCallback();
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
				Logger.debug(this, "Spurious file in channel directory: " + f);
				continue;
			}

			try {
				int num = Integer.parseInt(f.getName());
				if(num >= nextChannelNum.get()) {
					nextChannelNum.set(num + 1);
				}
			} catch(NumberFormatException e) {
				Logger.debug(this, "Found directory with malformed name: " + f);
				continue;
			}

			try {
				Channel channel = new Channel(f, FreemailPlugin.getExecutor(TaskType.UNSPECIFIED), new HighLevelFCPClient(), freemail, freemailAccount, ackCallback);
				channels.add(channel);
			} catch(ChannelTimedOutException e) {
				Logger.debug(this, "Deleting timed out channel");
				if(!Channel.deleteChannel(f)) {
					Logger.error(this, "Failed to delete channel because there are files left in " + f);
				}
			}
		}
	}

	public void start() {
		if(outbox.isDirectory()) {
			for(File rcptOutbox : outbox.listFiles()) {
				if(!rcptOutbox.isDirectory()) {
					Logger.debug(this, "Spurious file in outbox: " + rcptOutbox);
					continue;
				}

				for(File f : rcptOutbox.listFiles()) {
					if(!f.isFile()) {
						Logger.debug(this, "Spurious file in contact outbox: " + f);
						continue;
					}

					String identifier = f.getName();

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
						Logger.debug(this, "Found file without valid message number: " + f);
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
			InputStream messageStream = message.getInputStream();
			try {
				byte[] buffer = new byte[1024];
				while(true) {
					int read = messageStream.read(buffer, 0, buffer.length);
					if(read == -1) break;
					os.write(buffer, 0, read);
				}
			} finally {
				Closer.close(os);
				Closer.close(messageStream);
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
				channel = new Channel(newChannelDir, FreemailPlugin.getExecutor(TaskType.UNSPECIFIED), new HighLevelFCPClient(), freemail, freemailAccount, ackCallback);
			} catch(ChannelTimedOutException e) {
				//Can't happen since we're creating a new channel
				throw new AssertionError("Caugth ChannelTimedOutException when creating a new channel");
			}
			channel.setRemoteIdentity(remoteIdentity);
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
				channel = new Channel(newChannelDir, FreemailPlugin.getExecutor(TaskType.UNSPECIFIED), new HighLevelFCPClient(), freemail, freemailAccount, ackCallback);
			} catch(ChannelTimedOutException e) {
				//Can't happen since we're creating a new channel
				throw new AssertionError("Caugth ChannelTimedOutException when creating a new channel");
			}
			channel.setRemoteIdentity(remoteIdentity);
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
			subject = msg.getFirstHeader("Subject");
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
			Logger.debug(this, "SenderTask for message " + identifier + " on account " + freemailAccount.getIdentity() + " running");

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
				boolean inserted = sendMessage();
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
			ScheduledExecutorService senderExecutor = FreemailPlugin.getExecutor(TaskType.SENDER);
			tasks.put(identifier, senderExecutor.schedule(this, retryIn, TimeUnit.MILLISECONDS));
		}

		private boolean sendMessage() {
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
					Logger.error(this, "Caugth IOException while sending: " + e);
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
		@Override
		public void onAckReceived(String remote, long id) {
			File rcptOutbox;
			try {
				String base32Id = Base32.encode(Base64.decode(remote)).toLowerCase();
				rcptOutbox = new File(outbox, base32Id);
			} catch (IllegalBase64Exception e) {
				throw new AssertionError();
			}

			File message = new File(rcptOutbox, "" + id);
			if(!message.delete()) {
				Logger.error(this, "Couldn't delete " + message);
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
			File rcptOutbox;
			try {
				String base32Id = Base32.encode(Base64.decode(channel.getRemoteIdentity())).toLowerCase();
				rcptOutbox = new File(outbox, base32Id);
			} catch (IllegalBase64Exception e1) {
				throw new AssertionError();
			}
			if(!rcptOutbox.exists()) {
				rcptOutbox.mkdir();
			}
			MessageLog msgLog = new MessageLog(new File(rcptOutbox, MSG_LOG_NAME));
			boolean isDupe;
			try {
				isDupe = msgLog.isPresent(id);
			} catch (IOException ioe) {
				Logger.error(this,"Couldn't read logfile, so don't know whether received message is a duplicate or not. Leaving in the queue to try later.");
				return false;
			}
			if(isDupe) {
				Logger.normal(this,"Got a message, but we've already logged that message ID as received. Discarding.");
				return true;
			}

			try {
				storeMessage(message, freemailAccount.getMessageBank());
			} catch(IOException e) {
				return false;
			} catch(ConnectionTerminatedException e) {
				Logger.minor(this, "Couldn't store message because Freemail is shutting down, will try later");
				return false;
			}
			Logger.normal(this, "You've got mail!");
			try {
				msgLog.add(id, null);
			} catch(IOException e) {
				// how should we handle this? Remove the message from the inbox again?
				Logger.error(this,"warning: failed to write log file!");
			}

			return true;
		}
	}
}
