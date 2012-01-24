/*
 * FreemailAccount.java
 * This file is part of Freemail, copyright (C) 2008 Dave Baker
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

package freemail;

import java.io.File;

import org.archive.util.Base32;

import freemail.transport.MessageHandler;
import freemail.utils.PropsFile;
import freenet.support.Base64;
import freenet.support.IllegalBase64Exception;

public class FreemailAccount {
	private final String identity;
	private String nickname = null;
	private final File accdir;
	private final PropsFile accprops;
	private final MessageBank mb;
	private final MessageHandler messageHandler;
	
	FreemailAccount(String identity, File _accdir, PropsFile _accprops, Freemail freemail) {
		if(!FreenetURI.checkSSKHash(identity)) {
			throw new IllegalArgumentException("Expected valid identity string, but got " + identity);
		}
		try {
			Base64.decode(identity);
		} catch (IllegalBase64Exception e) {
			throw new IllegalArgumentException("Couldn't decode identity string: " + identity, e);
		}

		this.identity = identity;
		accdir = _accdir;
		accprops = _accprops;
		mb = new MessageBank(this);

		File channelDir = new File(accdir, "channel");
		messageHandler = new MessageHandler(new File(accdir, "outbox"), freemail, channelDir, this);
	}
	
	public void startTasks() {
		messageHandler.start();
	}

	public String getIdentity() {
		return identity;
	}

	public String getDomain() {
		try {
			return Base32.encode(Base64.decode(identity)).toLowerCase() + ".freemail";
		} catch(IllegalBase64Exception e) {
			//This would mean that WoT has changed the encoding of the identity string
			throw new AssertionError("Got IllegalBase64Exception when decoding " + identity);
		}
	}
	
	public File getAccountDir() {
		return accdir;
	}
	
	public PropsFile getProps() {
		return accprops;
	}
	
	public MessageBank getMessageBank() {
		return mb;
	}

	public synchronized String getNickname() {
		return nickname;
	}

	public synchronized void setNickname(String nickname) {
		this.nickname = nickname;
		synchronized(accprops) {
			accprops.put("nickname", nickname);
		}
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}
}
