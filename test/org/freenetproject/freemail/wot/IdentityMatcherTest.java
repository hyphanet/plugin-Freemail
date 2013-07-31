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

package org.freenetproject.freemail.wot;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.freenetproject.freemail.wot.Identity;
import org.freenetproject.freemail.wot.IdentityMatcher;
import org.freenetproject.freemail.wot.IdentityMatcher.MatchMethod;

import fakes.MockWoTConnection;
import freenet.pluginmanager.PluginNotFoundException;

public class IdentityMatcherTest {
	private static final Identity identity = new Identity("D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc", "SSK@D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc,xgddjFHx2S~5U6PeFkwqO5V~1gZngFLoM-xaoMKSBI8,AQACAAE", "zidel");

	@Test
	public void fullIdentityMatch() throws PluginNotFoundException {
		String recipient = identity.getNickname() + "@" + identity.getIdentityID() + ".freemail";
		EnumSet<MatchMethod> set = EnumSet.allOf(MatchMethod.class);

		runMatcherTest(recipient, set);
	}

	@Test
	public void fullBase32IdentityMatch() throws PluginNotFoundException {
		String recipient = identity.getNickname() + "@" + identity.getBase32IdentityID() + ".freemail";
		EnumSet<MatchMethod> set = EnumSet.of(MatchMethod.FULL_BASE32);

		runMatcherTest(recipient, set);
	}

	@Test
	public void fullBase32MatchWithOnlyId() throws PluginNotFoundException {
		String recipient = identity.getBase32IdentityID();
		EnumSet<MatchMethod> set = EnumSet.of(MatchMethod.FULL_BASE32);

		runMatcherTest(recipient, set);
	}

	private void runMatcherTest(String recipient, EnumSet<MatchMethod> methods) throws PluginNotFoundException {
		MockWoTConnection wotConnection = new MockWoTConnection(null, null);
		wotConnection.setTrustedIdentities(Collections.singleton(identity));
		wotConnection.setUntrustedIdentities(Collections.<Identity>emptySet());
		wotConnection.setOwnIdentities(Collections.<OwnIdentity>emptyList());

		IdentityMatcher identityMatcher = new IdentityMatcher(wotConnection);
		Set<String> recipients = Collections.singleton(recipient);
		Map<String, List<Identity>> matches;
		matches = identityMatcher.matchIdentities(recipients, identity.getIdentityID(), methods);

		assertEquals(1, matches.size());
		assertEquals(identity, matches.get(recipient).get(0));
	}

	@Test
	public void fullMatchWithPartialId() throws PluginNotFoundException {
		MockWoTConnection wotConnection = new MockWoTConnection(null, null);
		wotConnection.setTrustedIdentities(Collections.singleton(identity));
		wotConnection.setUntrustedIdentities(Collections.<Identity>emptySet());
		wotConnection.setOwnIdentities(Collections.<OwnIdentity>emptyList());

		IdentityMatcher identityMatcher = new IdentityMatcher(wotConnection);

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
}
