package org.freenetproject.freemail.test;

import java.util.Locale;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class OverrideLocaleTest {

	@Test
	public void localeIsNotChangedBeforeTestIsRun() {
		Locale.setDefault(new Locale("test2"));
		var overrideLocale = new OverrideLocale(new Locale("test"));
		overrideLocale.apply(new Statement() {
			@Override
			public void evaluate() {
			}
		}, Description.EMPTY);
		assertThat(Locale.getDefault(), equalTo(new Locale("test2")));
	}

	@Test
	public void localeIsChangedDuringTest() throws Throwable {
		var overrideLocale = new OverrideLocale(new Locale("test"));
		overrideLocale.apply(new Statement() {
			@Override
			public void evaluate() {
				assertThat(Locale.getDefault(), equalTo(new Locale("test")));
			}
		}, Description.EMPTY).evaluate();
	}

	@Test
	public void localeIsRestoredAfterTheTest() throws Throwable {
		Locale.setDefault(new Locale("test2"));
		var overrideLocale = new OverrideLocale(new Locale("test"));
		overrideLocale.apply(new Statement() {
			@Override
			public void evaluate() {
			}
		}, Description.EMPTY).evaluate();
		assertThat(Locale.getDefault(), equalTo(new Locale("test2")));
	}

}
