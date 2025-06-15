package org.freenetproject.freemail.ui.web;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class MessageToadletTest {

	@Test
	public void messageToadletIsNotLinkExcepted() throws URISyntaxException {
		MessageToadlet messageToadlet = new MessageToadlet(null, mock(), null);
		assertThat(messageToadlet.isLinkExcepted(new URI("/")), equalTo(false));
	}

}
