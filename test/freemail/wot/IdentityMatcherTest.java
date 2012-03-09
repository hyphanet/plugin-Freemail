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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import freemail.wot.Identity;
import freemail.wot.IdentityMatcher.MatchMethod;
import freemail.wot.OwnIdentity;
import freemail.wot.WoTConnection;
import freenet.pluginmanager.PluginNotFoundException;

import junit.framework.TestCase;

public class IdentityMatcherTest extends TestCase {
	private static final Identity identity = new Identity("D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc", "SSK@D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc,xgddjFHx2S~5U6PeFkwqO5V~1gZngFLoM-xaoMKSBI8,AQACAAE", "zidel");

	public void testFullIdentityMatch() throws PluginNotFoundException {
		String recipient = identity.getNickname() + "@" + identity.getIdentityID() + ".freemail";
		EnumSet<MatchMethod> set = EnumSet.allOf(MatchMethod.class);

		runMatcherTest(recipient, set);
	}

	public void testFullBase32IdentityMatch() throws PluginNotFoundException {
		String recipient = identity.getNickname() + "@" + identity.getBase32IdentityID() + ".freemail";
		EnumSet<MatchMethod> set = EnumSet.of(MatchMethod.FULL_BASE32);

		runMatcherTest(recipient, set);
	}

	public void testFullBase32MatchWithOnlyId() throws PluginNotFoundException {
		String recipient = identity.getBase32IdentityID();
		EnumSet<MatchMethod> set = EnumSet.of(MatchMethod.FULL_BASE32);

		runMatcherTest(recipient, set);
	}

	private void runMatcherTest(String recipient, EnumSet<MatchMethod> methods) throws PluginNotFoundException {
		IdentityMatcher identityMatcher = new IdentityMatcher(new FakeWoTConnection());
		Set<String> recipients = Collections.singleton(recipient);
		Map<String, List<Identity>> matches;
		matches = identityMatcher.matchIdentities(recipients, identity.getIdentityID(), methods);

		assertEquals(1, matches.size());
		assertEquals(identity, matches.get(recipient).get(0));
	}

	public void testFullMatchWithPartialId() throws PluginNotFoundException {
		IdentityMatcher identityMatcher = new IdentityMatcher(new FakeWoTConnection());

		//Check an identity string that is missing the last character
		String id = identity.getBase32IdentityID();
		id = id.substring(0, id.length() - 1);
		id = identity.getNickname() + "@" + id + ".freemail";
		Set<String> recipients = Collections.singleton(id);

		EnumSet<MatchMethod> set = EnumSet.of(MatchMethod.FULL_BASE32);

		Map<String, List<Identity>> matches;
		matches = identityMatcher.matchIdentities(recipients, identity.getIdentityID(), set);

		assertEquals(1, matches.size());
		assertEquals(0, matches.get(id).size());
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
