/*
 * FreemailAccountTest.java
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

package org.freenetproject.freemail;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

import org.archive.util.Base32;

import freenet.support.Base64;

public class FreemailAccountTest {
	/**
	 * Checks for the bug fixed in de75dc62b0bbf5bb7d547987803b2a4ed5f098a9 that made the domain
	 * contain the dotless i instead of the normal i when running with a Turkish locale.
	 */
	@Test
	public void simpleAddress() {
		Locale defaultLocale = Locale.getDefault();
		Locale.setDefault(new Locale("tr"));
		try {
			final String base32_id = "73nlovzsg6aof24rkiaju3tled2ipvxtl6fhcrlcbbx7de2mbxuq";
			final String base64_id = Base64.encode(Base32.decode(base32_id));
			FreemailAccount account = new FreemailAccount(base64_id, null, null, null);
			assertEquals(base32_id + ".freemail", account.getDomain());
		} finally {
			Locale.setDefault(defaultLocale);
		}
	}
}
