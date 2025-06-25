package org.freenetproject.freemail.ui.web;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class AddAccountToadletTest {

	@Test
	public void addAccountToadletIsNotLinkFilterExcepted() throws URISyntaxException {
		AddAccountToadlet toadloet = new AddAccountToadlet(mock(), null, null, null);
		assertThat(toadloet.isLinkExcepted(new URI("/")), equalTo(false));
	}

}
