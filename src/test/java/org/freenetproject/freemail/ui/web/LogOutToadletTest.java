package org.freenetproject.freemail.ui.web;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class LogOutToadletTest {

	@Test
	public void logoutToadletIsNotLinkExcepted() throws URISyntaxException {
		LogOutToadlet logOutToadlet = new LogOutToadlet(mock(), null);
		assertThat(logOutToadlet.isLinkExcepted(new URI("/")), equalTo(false));
	}

}
