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
