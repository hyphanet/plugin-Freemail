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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import freemail.utils.Logger;
import freemail.utils.PropsFile;
import freemail.wot.Identity;
import freenet.support.api.Bucket;
import freenet.support.io.Closer;

/**
 * MessageHandler is the high level interface to the part of Freemail that sends messages over
 * Freenet. The general contract of MessageHandler is that submitted messages are either
 * delivered successfully to the recipient, or, for messages that can not be delivered, a failure
 * notice is delivered to the inbox of the sender. Messages received over Freenet are delivered to
 * the inbox of the account that is specified during construction.
 */
public class MessageHandler {
	private final static String INDEX_NAME = "index";

	/**
	 * Holds the static portions of the keys used in the index file. The values that are stored per
	 * message must be appended to the message number.
	 */
	private static class IndexKeys {
		/** Comma separated list of recipient ids that this message should be sent to */
		private static final String RECIPIENT = ".recipient";
		/** The message number that should be used for the next message that is submitted */
		private static final String NEXT_MESSAGE_NUMBER = "nextMessageNumber";
	}

	private final File outbox;
	private final PropsFile index;
	private final AtomicInteger nextMessageNum = new AtomicInteger();

	public MessageHandler(File outbox) {
		this.outbox = outbox;
		index = PropsFile.createPropsFile(new File(outbox, INDEX_NAME));

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
		}

		return true;
	}

	private int getMessageNumber() {
		int number = nextMessageNum.getAndIncrement();
		synchronized(index) {
			index.put(IndexKeys.NEXT_MESSAGE_NUMBER, "" + (number + 1));
		}
		return number;
	}
}
