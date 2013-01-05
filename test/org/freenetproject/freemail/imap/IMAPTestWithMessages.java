/*
 * IMAPTestWithMessages.java
 * This file is part of Freemail
 * Copyright (C) 2011,2012 Martin Nyhus
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

package org.freenetproject.freemail.imap;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.MailMessage;

import fakes.ConfigurableAccountManager;

/**
 * IMAP test template that adds messages to the inbox before running tests
 */
public abstract class IMAPTestWithMessages extends IMAPTestBase {
	protected static final List<String> INITIAL_RESPONSES;
	static {
		List<String> backing = new LinkedList<String>();
		backing.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		backing.add("0001 OK Logged in");
		backing.add("* FLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)");
		backing.add("* OK [PERMANENTFLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)] Limited");
		backing.add("* 9 EXISTS");
		backing.add("* 9 RECENT");
		backing.add("* OK [UIDVALIDITY 1] Ok");
		backing.add("0002 OK [READ-WRITE] Done");
		INITIAL_RESPONSES = Collections.unmodifiableList(backing);
	}

	@Override
	public void before() {
		super.before();

		//Add a few messages to the inbox
		AccountManager temp = new ConfigurableAccountManager(accountManagerDir, false, accountDirs);
		FreemailAccount account = temp.authenticate(BASE64_USERNAME, "");
		for(int i = 0; i < 10; i++) {
			MailMessage m = account.getMessageBank().createMessage();
			m.addHeader("Subject", "IMAP test message " + i);
			try {
				m.writeHeadersAndGetStream();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				fail(e.toString());
			}
			m.commit();
		}

		//Delete message 5 so there will be a gap in the UIDs
		account.getMessageBank().listMessages().get(5).delete();
	}
}
