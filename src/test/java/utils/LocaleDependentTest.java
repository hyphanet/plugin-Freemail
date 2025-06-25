/*
 * LocaleDependentTest.java
 * This file is part of Freemail
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocaleDependentTest {
	private Locale original;
	private Locale locale;

	public LocaleDependentTest(Locale locale) {
		this.locale = locale;
	}

	public static List<Locale> data() {
		return Arrays.asList(Locale.getAvailableLocales());
	}

	public void before() {
		original = Locale.getDefault();
		Locale.setDefault(locale);
	}

	public void after() {
		Locale.setDefault(original);
	}
}
