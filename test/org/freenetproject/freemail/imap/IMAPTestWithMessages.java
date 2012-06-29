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

import java.io.FileNotFoundException;

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.MailMessage;

import fakes.ConfigurableAccountManager;

/**
 * IMAP test template that adds messages to the inbox before running tests
 */
public abstract class IMAPTestWithMessages extends IMAPTestBase {
	@Override
	public void setUp() {
		super.setUp();

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
	}
}
