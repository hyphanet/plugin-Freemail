/*
 * IdentityMatcherTest.java
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

package freemail.wot;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import freemail.wot.Identity;
import freemail.wot.OwnIdentity;
import freemail.wot.WoTConnection;
import freenet.pluginmanager.PluginNotFoundException;

import junit.framework.TestCase;

public class IdentityMatcherTest extends TestCase {
	private static final Identity identity = new Identity("D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc", "SSK@D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc,xgddjFHx2S~5U6PeFkwqO5V~1gZngFLoM-xaoMKSBI8,AQACAAE", "zidel");

	public void testFullIdentityMatch() throws PluginNotFoundException {
		IdentityMatcher identityMatcher = new IdentityMatcher(new FakeWoTConnection());

		String recipient = identity.getNickname() + "@" + identity.getIdentityID() + ".freemail";
		Set<String> recipients = new HashSet<String>();
		recipients.add(recipient);

		EnumSet<IdentityMatcher.MatchMethod> set = EnumSet.allOf(IdentityMatcher.MatchMethod.class);
		Map<String, List<Identity>> matches = identityMatcher.matchIdentities(recipients, identity.getIdentityID(), set);

		assert (matches.size() == 1);
		assert (matches.get(recipient).equals(identity));
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
