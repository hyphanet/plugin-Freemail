/*
 * NullAccountManager.java
 * This file is part of Freemail, copyright (C) 2011 Martin Nyhus
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

package fakes;

import java.io.File;
import java.util.List;

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.Freemail;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.wot.OwnIdentity;


public class NullAccountManager extends AccountManager {
	public NullAccountManager(File datadir, Freemail freemail) {
		super(datadir, freemail);
	}

	@Override
	public void addIdentities(List<OwnIdentity> oids) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FreemailAccount authenticate(String username, String password) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FreemailAccount getAccount(String username) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<FreemailAccount> getAllAccounts() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void startTasks() {
		throw new UnsupportedOperationException();
	}
}
