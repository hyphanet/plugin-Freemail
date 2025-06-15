package org.freenetproject.freemail.ui.web;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class SettingsToadletTest {

	@Test
	public void settingsToadletIsNotLinkExcepted() throws URISyntaxException {
		SettingsToadlet settingsToadlet = new SettingsToadlet(mock(), null, null, null);
		assertThat(settingsToadlet.isLinkExcepted(new URI("/")), equalTo(false));
	}

}
