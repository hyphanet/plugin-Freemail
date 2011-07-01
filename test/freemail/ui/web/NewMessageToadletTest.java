/*
 * NewMessageToadletTest.java
 * This file is part of Freemail
 * Copyright (C) 2011 Martin Nyhus
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

package freemail.ui.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import freemail.wot.Identity;
import freemail.wot.OwnIdentity;
import freemail.wot.WoTConnection;

import junit.framework.TestCase;

public class NewMessageToadletTest extends TestCase {
	private static final Identity identity = new Identity("D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc", "SSK@D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc,xgddjFHx2S~5U6PeFkwqO5V~1gZngFLoM-xaoMKSBI8,AQACAAE", "zidel");

	public void testFullIdentityMatch() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method m = NewMessageToadlet.class.getDeclaredMethod("matchIdentities", Set.class, String.class);
		m.setAccessible(true);

		NewMessageToadlet toadlet = new NewMessageToadlet(null, null, null, new FakeWoTConnection(), null);
		Set<String> set = new HashSet<String>();
		set.add(identity.getNickname() + "@" + identity.getIdentityID() + ".freemail");

		@SuppressWarnings("unchecked")
		Set<Identity> matches = (Set<Identity>) m.invoke(toadlet, set, identity.getIdentityID());

		Iterator<Identity> iterator = matches.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(identity, iterator.next());
		assertFalse(iterator.hasNext());
	}

	private class FakeWoTConnection implements WoTConnection {
		@Override
		public List<OwnIdentity> getAllOwnIdentities() {
			return new LinkedList<OwnIdentity>();
		}

		@Override
		public Set<Identity> getAllTrustedIdentities(String trusterId) {
			Set<Identity> set = new HashSet<Identity>();
			set.add(identity);
			return set;
		}

		@Override
		public Set<Identity> getAllUntrustedIdentities(String trusterId) {
			return new HashSet<Identity>();
		}

		@Override
		public Identity getIdentity(String identityID, String trusterID) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean setProperty(String identityID, String key, String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getProperty(String identityID, String key) {
			throw new UnsupportedOperationException();
		}
	}
}
