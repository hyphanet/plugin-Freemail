package org.freenetproject.freemail.ui.web;

import org.freenetproject.freemail.FreemailAccount;

import freenet.clients.http.SessionManager.Session;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContext;

public class LoginManager {
	private final SessionManager sessionManager;

	public LoginManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public Session getSession(ToadletContext ctx) {
		return sessionManager.useSession(ctx);
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
