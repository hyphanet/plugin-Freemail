package org.freenetproject.freemail.ui.web;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class InboxToadletTest {

	@Test
	public void inboxToadletIsNotLinkExcepted() throws URISyntaxException {
		InboxToadlet inboxToadlet = new InboxToadlet(null, mock(), null);
		assertThat(inboxToadlet.isLinkExcepted(new URI("/")), equalTo(false));
	}

}
