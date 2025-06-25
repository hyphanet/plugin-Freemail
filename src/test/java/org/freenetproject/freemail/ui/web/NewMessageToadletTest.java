package org.freenetproject.freemail.ui.web;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class NewMessageToadletTest {

	@Test
	public void newMessageToadletForUriWithoutToParameterIsNotLinkExcepted() throws URISyntaxException {
		NewMessageToadlet newMessageToadlet = new NewMessageToadlet(null, null, mock(), null);
		assertThat(newMessageToadlet.isLinkExcepted(new URI("/")), equalTo(false));
	}

	@Test
	public void newMessageToadletForUriWithToParameterAsFirstParameterIsLinkExcepted() throws URISyntaxException {
		NewMessageToadlet newMessageToadlet = new NewMessageToadlet(null, null, mock(), null);
		assertThat(newMessageToadlet.isLinkExcepted(new URI("/Freemail/NewMessage?to=some-identity")), equalTo(true));
	}

	@Test
	public void newMessageToadletForUriWithToParameterAsSecondParameterIsLinkExcepted() throws URISyntaxException {
		NewMessageToadlet newMessageToadlet = new NewMessageToadlet(null, null, mock(), null);
		assertThat(newMessageToadlet.isLinkExcepted(new URI("/Freemail/NewMessage?foo=bar&to=some-identity")), equalTo(true));
	}

	@Test
	public void newMessageToadletForUriWithHithertoParameterIsNotLinkExcepted() throws URISyntaxException {
		NewMessageToadlet newMessageToadlet = new NewMessageToadlet(null, null, mock(), null);
		assertThat(newMessageToadlet.isLinkExcepted(new URI("/Freemail/NewMessage?hitherto=some-identity")), equalTo(false));
	}

}
