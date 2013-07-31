/*
 * LoginManager.java
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

package org.freenetproject.freemail.ui.web;

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.FreemailAccount;

import freenet.clients.http.SessionManager.Session;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContext;

public class LoginManager {
	private final AccountManager accountManager;
	private final SessionManager sessionManager;

	public LoginManager(AccountManager accountManager, SessionManager sessionManager) {
		this.accountManager = accountManager;
		this.sessionManager = sessionManager;
	}

	public Session getSession(ToadletContext ctx) {
		Session s = sessionManager.useSession(ctx);

		if((s == null) && (accountManager.getAllAccounts().size() == 1)) {
			//Auto-login if there is only one account
			FreemailAccount acc = accountManager.getAllAccounts().get(0);
			s = sessionManager.createSession(acc.getIdentity(), ctx);
		}

		return s;
	}

	public Session createSession(ToadletContext ctx, FreemailAccount account) {
		return sessionManager.createSession(account.getIdentity(), ctx);
	}

	public boolean sessionExists(ToadletContext ctx) {
		return getSession(ctx) != null;
	}

	public void deleteSession(ToadletContext ctx) {
		sessionManager.deleteSession(ctx);
	}
}
