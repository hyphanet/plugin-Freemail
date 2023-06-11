/*
 * MockWoTConnection.java
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.wot.Identity;
import org.freenetproject.freemail.wot.OwnIdentity;
import org.freenetproject.freemail.wot.WoTConnection;

import freenet.pluginmanager.PluginNotFoundException;

public class MockWoTConnection implements WoTConnection {
	private final Map<String, Map<String, Identity>> identityMap;
	private final Map<String, Map<String, String>> properties;

	private List<OwnIdentity> ownIdentities = null;
	private Set<Identity> trustedIdentities = null;
	private Set<Identity> untrustedIdentities = null;

	public MockWoTConnection(Map<String, Map<String, Identity>> identityMap, Map<String, Map<String, String>> properties) {
		this.identityMap = identityMap;
		this.properties = properties;
	}


	/*
	 * Functions for controlling the return value of the overridden ones
	 */

	public void setOwnIdentities(List<OwnIdentity> ownIdentities) {
		this.ownIdentities = ownIdentities;
	}

	public void setTrustedIdentities(Set<Identity> trustedIdentities) {
		this.trustedIdentities = trustedIdentities;
	}

	public void setUntrustedIdentities(Set<Identity> untrustedIdentities) {
		this.untrustedIdentities = untrustedIdentities;
	}


	/*
	 * The overridden functions
	 */

	@Override
	public List<OwnIdentity> getAllOwnIdentities() throws PluginNotFoundException {
		return ownIdentities;
	}

	@Override
	public Set<Identity> getAllIdentities() throws PluginNotFoundException {
		Set<Identity> allIdentities = new HashSet<Identity>();
		if (trustedIdentities != null)
			allIdentities.addAll(trustedIdentities);
		if (untrustedIdentities != null)
			allIdentities.addAll(untrustedIdentities);
		return allIdentities;
	}

	@Override
	public Identity getIdentity(String identity, String truster) throws PluginNotFoundException {
		Logger.debug(this, "getIdentity(identity=" + identity + ", truster=" + truster + ")");

		if(identityMap == null) {
			return null;
		}

		if(!identityMap.containsKey(truster)) {
			return null;
		}

		Map<String, Identity> identities = identityMap.get(truster);
		if(!identities.containsKey(identity)) {
			return null;
		}

		return identities.get(identity);
	}

	@Override
	public boolean setProperty(String identity, String key, String value) throws PluginNotFoundException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProperty(String identity, String key) throws PluginNotFoundException {
		Logger.debug(this, "getProperty(identity=" + identity + ", key=" + key + ")");

		if(properties == null) {
			return null;
		}

		if(!properties.containsKey(identity)) {
			return null;
		}

		Map<String, String> identities = properties.get(identity);
		if(!identities.containsKey(key)) {
			return null;
		}

		return identities.get(key);
	}

	@Override
	public boolean setContext(String identity, String context) throws PluginNotFoundException {
		throw new UnsupportedOperationException();
	}
}
