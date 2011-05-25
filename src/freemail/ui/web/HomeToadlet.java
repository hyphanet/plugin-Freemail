package freemail.ui.web;

import java.io.IOException;
import java.net.URI;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.api.HTTPRequest;

public class HomeToadlet extends WebPage {
	public HomeToadlet(HighLevelSimpleClient client) {
		super(client);
	}

	@Override
	public String path() {
		return "/Freemail/";
	}

	@Override
	LinkEnabledCallback getLinkEnabledCallback() {
		return new LinkEnabledCallback() {
			@Override
			public boolean isEnabled(ToadletContext ctx) {
				return true;
			}
		};
	}

	@Override
	public void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		writeHTMLReply(ctx, 200, "OK", "<html><body>This is the home page</body></html>");
	}

	@Override
	public void handleMethodPOST(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		handleMethodGET(uri, req, ctx);
	}
}
