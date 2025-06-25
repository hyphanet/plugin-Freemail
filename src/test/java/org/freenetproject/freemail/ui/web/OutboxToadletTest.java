package org.freenetproject.freemail.ui.web;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class OutboxToadletTest {

	@Test
	public void outboxToadletIsNotLinkExcepted() throws URISyntaxException {
		OutboxToadlet outboxToadlet = new OutboxToadlet(mock(), null, null, null);
		assertThat(outboxToadlet.isLinkExcepted(new URI("/")), equalTo(false));
	}

}
