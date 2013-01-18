/*
 * NullFreemailAccount.java
 * This file is part of Freemail
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

package org.freenetproject.freemail;

import java.io.File;

import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.transport.MessageHandler;
import org.freenetproject.freemail.utils.PropsFile;

public class NullFreemailAccount extends FreemailAccount {
	public NullFreemailAccount(String identity, File accountDir, PropsFile propsFile, Freemail freemail) {
		super(identity, accountDir, propsFile, freemail);
	}

	@Override
	public void startTasks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getIdentity() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDomain() {
		throw new UnsupportedOperationException();
	}

	@Override
	public File getAccountDir() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PropsFile getProps() {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageBank getMessageBank() {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized String getNickname() {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void setNickname(String nickname) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHandler getMessageHandler() {
		throw new UnsupportedOperationException();
	}
}
