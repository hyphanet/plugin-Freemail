package org.freenetproject.freemail.ui.web;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WebPageTest {

	@Test
	public void defaultWebPageIsNotLinkExcepted() throws URISyntaxException {
		WebPage webPage = new TestWebPage(mock(), null);
		assertThat(webPage.isLinkExcepted(new URI("/")), equalTo(false));
	}

	@Test
	public void templateCanBeLoadedFromClasspath() throws IOException, ToadletContextClosedException {
		WebPage webPage = new TestWebPage(mock(), null) {
			@Override
			HTTPResponse makeWebPageGet(URI uri, HTTPRequest httpRequest, ToadletContext toadletContext, PageNode pageNode) throws IOException {
				Map<String, Object> testMap = new HashMap<>();
				testMap.put("test", "Test!");
				HTMLNode htmlNode = new HTMLNode("#");
				addChild(htmlNode, "test", testMap);

				return new GenericHTMLResponse(toadletContext, 200, "OK", htmlNode.generate());
			}
		};
		ToadletContext toadletContext = mock();
		webPage.makeWebPageGet(null, null, toadletContext, null).writeResponse();
		verify(toadletContext).sendReplyHeaders(eq(200), any(), any(), startsWith("text/html"), anyLong(), anyBoolean());
		verify(toadletContext).writeData("<html>Test!</html>\n".getBytes(UTF_8), 0, 19);
	}

	private static class TestWebPage extends WebPage {

		private TestWebPage(PluginRespirator pluginRespirator, LoginManager loginManager) {
			super(pluginRespirator, loginManager);
		}

		@Override
		HTTPResponse makeWebPageGet(URI uri, HTTPRequest httpRequest, ToadletContext toadletContext, PageNode pageNode) throws IOException {
			return null;
		}

		@Override
		HTTPResponse makeWebPagePost(URI uri, HTTPRequest httpRequest, ToadletContext toadletContext, PageNode pageNode) throws IOException {
			return null;
		}

		@Override
		boolean requiresValidSession() {
			return false;
		}

		@Override
		public String path() {
			return "";
		}

	}

}
