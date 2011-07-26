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

import java.io.IOException;
import java.util.List;

import freemail.wot.Identity;
import freenet.support.api.Bucket;

/**
 * MessageHandler is the high level interface to the part of Freemail that sends messages over
 * Freenet. The general contract of MessageHandler is that submitted messages are either
 * delivered successfully to the recipient, or, for messages that can not be delivered, a failure
 * notice is delivered to the inbox of the sender. Messages received over Freenet are delivered to
 * the inbox of the account that is specified during construction.
 */
public class MessageHandler {
	public boolean sendMessage(List<Identity> recipients, Bucket message) throws IOException {
		throw new UnsupportedOperationException();
	}
}
