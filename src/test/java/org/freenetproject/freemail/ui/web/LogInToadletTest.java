package org.freenetproject.freemail.ui.web;

import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class LogInToadletTest {

	@Test
	public void loginToadletIsNotLinkExcepted() throws URISyntaxException {
		LogInToadlet logInToadlet = new LogInToadlet(mock(), null, null);
		assertThat(logInToadlet.isLinkExcepted(new URI("/")), equalTo(false));
	}

}
