/*
 * FreenetURITest.java
 * This file is part of Freemail, copyright (C) 2012 Martin Nyhus
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

package freemail;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class FreenetURITest extends TestCase {
	private static final String KEY_HASH = "TJl1G~HtSb5uRWW2ei36yXbilXTehZwXNwTirvpVSQ";
	private static final String KEY_BODY = KEY_HASH + ",ISYik-w5cLR7n6IzL3GjmHmp~tj7AJaDWtNhrZ5qt-4,AQECAAE";

	private static final List<String> validSSKs = new LinkedList<String>();
	private static final List<String> validUSKs = new LinkedList<String>();
	static {
		validSSKs.add("SSK@" + KEY_BODY);
		validSSKs.add("SSK@" + KEY_BODY + "/");
		validSSKs.add("SSK@" + KEY_BODY + "/testsite");
		validSSKs.add("SSK@" + KEY_BODY + "/testsite-1");
		validSSKs.add("SSK@" + KEY_BODY + "/testsite/");
		validSSKs.add("SSK@" + KEY_BODY + "/testsite-1/");
		validSSKs.add("SSK@" + KEY_BODY + "/testsite/file");
		validSSKs.add("SSK@" + KEY_BODY + "/testsite-1/file");

		validUSKs.add("USK@" + KEY_BODY);
		validUSKs.add("USK@" + KEY_BODY + "/");
		validUSKs.add("USK@" + KEY_BODY + "/testsite");
		validUSKs.add("USK@" + KEY_BODY + "/testsite/1");
		validUSKs.add("USK@" + KEY_BODY + "/testsite/1/");
		validUSKs.add("USK@" + KEY_BODY + "/testsite/1/file");
		validUSKs.add("USK@" + KEY_BODY + "/testsite/-1");
		validUSKs.add("USK@" + KEY_BODY + "/testsite/-1/");
		validUSKs.add("USK@" + KEY_BODY + "/testsite/-1/file");
	}

	public void testCheckSSK() {
		for(String key : validSSKs) {
			assertTrue("SSK check failed for " + key, FreenetURI.checkSSK(key));
		}
	}

	public void testCheckUSK() {
		for(String key : validUSKs) {
			assertTrue("USK check failed for " + key, FreenetURI.checkUSK(key));
		}
	}

	public void testCheckSSKHash() {
		assertTrue("SSK hash check failed for " + KEY_HASH, FreenetURI.checkSSKHash(KEY_HASH));
	}
}
