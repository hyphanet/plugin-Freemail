package org.freenetproject.freemail.ui.web;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class InfoToadletTest {

	@Test
	public void infoToadletIsNotLinkExcepted() throws URISyntaxException {
		InfoToadlet infoToadlet = new InfoToadlet(mock(), null, null, null);
		assertThat(infoToadlet.isLinkExcepted(new URI("/")), equalTo(false));
	}

}
