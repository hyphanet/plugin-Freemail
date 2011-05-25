package freemail.ui.web;

import java.io.IOException;
import java.net.URI;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.api.HTTPRequest;

abstract class WebPage extends Toadlet {
	WebPage(HighLevelSimpleClient client) {
		super(client);
	}

	abstract LinkEnabledCallback getLinkEnabledCallback();

	//All web pages must be able to handle both get and post (even if that means eg. redirecting
	//post to get).
	public abstract void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException;
	public abstract void handleMethodPOST(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException;
}
