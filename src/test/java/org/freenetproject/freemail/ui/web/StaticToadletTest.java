package org.freenetproject.freemail.ui.web;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class StaticToadletTest {

	@Test
	public void staticToadletIsNotLinkExcepted() throws URISyntaxException {
		StaticToadlet staticToadlet = new StaticToadlet(mock(), null);
		assertThat(staticToadlet.isLinkExcepted(new URI("/")), equalTo(false));
	}

}
