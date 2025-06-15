package org.freenetproject.freemail.ui.web;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.support.api.HTTPRequest;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class WebPageTest {

	@Test
	public void defaultWebPageIsNotLinkExcepted() throws URISyntaxException {
		WebPage webPage = new WebPage(mock(), null) {

			@Override
			public String path() {
				return "";
			}

			@Override
			HTTPResponse makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException {
				return null;
			}

			@Override
			HTTPResponse makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException {
				return null;
			}

			@Override
			boolean requiresValidSession() {
				return false;
			}
		};
		assertThat(webPage.isLinkExcepted(new URI("/")), equalTo(false));
	}

}
