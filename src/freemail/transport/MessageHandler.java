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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

import freemail.Freemail;
import freemail.FreemailAccount;
import freemail.FreemailPlugin;
import freemail.Postman;
import freemail.fcp.ConnectionTerminatedException;
import freemail.fcp.HighLevelFCPClient;
import freemail.transport.Channel.ChannelEventCallback;
import freemail.utils.Logger;
import freemail.utils.PropsFile;
import freemail.wot.Identity;
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
	private final static String LOG_DIR_NAME = "logs";

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
	}

	private final File outbox;
	private final PropsFile index;
	private final AtomicInteger nextMessageNum = new AtomicInteger();
	private final List<Channel> channels = new LinkedList<Channel>();
	private final Freemail freemail;
	private final File channelDir;
	private final FreemailAccount freemailAccount;
	private final AtomicInteger nextChannelNum = new AtomicInteger();
	private final ScheduledExecutorService executor;
	private final AckCallback ackCallback = new AckCallback();
	private final ConcurrentHashMap<Integer, Future<?>> tasks = new ConcurrentHashMap<Integer, Future<?>>();

	public MessageHandler(ScheduledExecutorService executor, File outbox, Freemail freemail, File channelDir, FreemailAccount freemailAccount) {
		this.outbox = outbox;
		index = PropsFile.createPropsFile(new File(outbox, INDEX_NAME));
		this.freemail = freemail;
		this.channelDir = channelDir;
		this.freemailAccount = freemailAccount;
		this.executor = executor;

		//Initialize nextMessageNum
		synchronized(index) {
			int messageNumber;
			try {
				messageNumber = Integer.parseInt(index.get(IndexKeys.NEXT_MESSAGE_NUMBER));
			} catch(NumberFormatException e) {
				messageNumber = 0;
			}
			nextMessageNum.set(messageNumber);
		}

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
				Channel channel = new Channel(f, FreemailPlugin.getExecutor(), new HighLevelFCPClient(), freemail, freemailAccount, ackCallback);
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
			for(File f : outbox.listFiles()) {
				if(!f.isFile()) {
					Logger.debug(this, "Spurious file in outbox: " + f);
					continue;
				}

				try {
					int num = Integer.parseInt(f.getName());
					Logger.debug(this, "Scheduling SenderTask for " + num);
					tasks.put(Integer.valueOf(num), executor.schedule(new SenderTask(num), 0, TimeUnit.NANOSECONDS));
				} catch(NumberFormatException e) {
					Logger.debug(this, "Found file with malformed name: " + f);
					continue;
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
			int msgNum = getMessageNumber();
			File messageFile = new File(outbox, "" + msgNum);

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

			synchronized(index) {
				index.put(msgNum + IndexKeys.RECIPIENT, recipient.getIdentityID());
			}

			tasks.put(Integer.valueOf(msgNum), executor.submit(new SenderTask(msgNum)));
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
				channel = new Channel(newChannelDir, FreemailPlugin.getExecutor(), new HighLevelFCPClient(), freemail, freemailAccount, ackCallback);
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
				channel = new Channel(newChannelDir, FreemailPlugin.getExecutor(), new HighLevelFCPClient(), freemail, freemailAccount, ackCallback);
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

	private int getMessageNumber() {
		int number = nextMessageNum.getAndIncrement();
		synchronized(index) {
			index.put(IndexKeys.NEXT_MESSAGE_NUMBER, "" + (number + 1));
		}
		return number;
	}

	private class SenderTask implements Runnable {
		private final int msgNum;

		private SenderTask(int msgNum) {
			this.msgNum = msgNum;
		}

		@Override
		public void run() {
			Logger.debug(this, "SenderTask for message " + msgNum + " on account " + freemailAccount.getUsername() + " running");

			//FIXME: This will be terribly inefficient if sendMessage takes a while since it will
			//       block all other operations as well. Only the id that is being used should
			//       be locked.
			long retryIn;
			synchronized(index) {
				long lastSendTime;
				try {
					String time = index.get(msgNum + IndexKeys.LAST_SEND_TIME);
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
						String firstSentTime = index.get(msgNum + IndexKeys.FIRST_SEND_TIME);
						if(firstSentTime == null) {
							index.put(msgNum + IndexKeys.FIRST_SEND_TIME, "" + System.currentTimeMillis());
						}
						index.put(msgNum + IndexKeys.LAST_SEND_TIME, "" + System.currentTimeMillis());

						retryIn = RESEND_TIME;
					}
				}
			}

			//Schedule again when the resend is due
			tasks.put(Integer.valueOf(msgNum), executor.schedule(this, retryIn, TimeUnit.MILLISECONDS));
		}

		private boolean sendMessage() {
			String recipient = index.get(msgNum + IndexKeys.RECIPIENT);

			Channel c;
			boolean inserted;
			while(true) {
				c = getChannel(recipient);
				Bucket message = new FileBucket(new File(outbox, "" + msgNum), false, false, false, false, false);
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

	private void deleteIndexEntries(long msgNum) {
		synchronized(index) {
			index.remove(msgNum + IndexKeys.FIRST_SEND_TIME);
			index.remove(msgNum + IndexKeys.LAST_SEND_TIME);
			index.remove(msgNum + IndexKeys.RECIPIENT);
		}
	}

	private static class MessageLog {
		private final File logfile;

		public MessageLog(File logFile) {
			this.logfile = logFile;
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

	private class AckCallback extends Postman implements ChannelEventCallback {
		@Override
		public void onAckReceived(long id) {
			File message = new File(outbox, "" + id);
			if(!message.delete()) {
				Logger.error(this, "Couldn't delete " + message);
			}

			deleteIndexEntries(id);

			Future<?> task = tasks.remove(Long.valueOf(id));
			if(task != null) {
				//Stop the insert if possible, but don't interrupt since the FCP code ignores it
				task.cancel(false);
			}
		}

		@Override
		public boolean handleMessage(Channel channel, BufferedReader message, int id) {
			File logDir = new File(outbox, LOG_DIR_NAME);
			if(!logDir.exists()) {
				logDir.mkdir();
			}
			MessageLog msgLog = new MessageLog(new File(logDir, channel.getRemoteIdentity()));
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
				msgLog.add(id);
			} catch(IOException e) {
				// how should we handle this? Remove the message from the inbox again?
				Logger.error(this,"warning: failed to write log file!");
			}

			return true;
		}
	}
}
