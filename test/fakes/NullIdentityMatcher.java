/*
 * NullIdentityMatcher.java
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

package fakes;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.freenetproject.freemail.wot.Identity;
import org.freenetproject.freemail.wot.IdentityMatcher;
import org.freenetproject.freemail.wot.WoTConnection;

import freenet.pluginmanager.PluginNotFoundException;

public class NullIdentityMatcher extends IdentityMatcher {
	public NullIdentityMatcher() {
		super(null);
	}

	public NullIdentityMatcher(WoTConnection wotConnection) {
		super(wotConnection);
	}

	@Override
	public Map<String, List<Identity>> matchIdentities(Set<String> recipients, String wotOwnIdentity, EnumSet<MatchMethod> methods) throws PluginNotFoundException {
		throw new UnsupportedOperationException();
	}
}
