package org.freenetproject.freemail.test;

import java.util.Locale;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit {@link TestRule} implementation that overrides the {@link
 * Locale#getDefault() default locale} during a test.
 * <h2>Usage</h2>
 * <pre>
 * public class FooTest {
 *     &#x40;Rule public final OverrideLocale overrideLocale = new OverrideLocale(Locale.FRENCH);
 *     &#x40;Test public void testLocale() {
 *         assertThat(Locale.getDefault(), equalTo(Locale.FRENCH));
 *     }
 * }
 * </pre>
 */
public class OverrideLocale implements TestRule {

	public OverrideLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				var previousLocale = Locale.getDefault();
				Locale.setDefault(locale);
				try {
					base.evaluate();
				} finally {
					Locale.setDefault(previousLocale);
				}
			}
		};
	}

	private final Locale locale;

}
