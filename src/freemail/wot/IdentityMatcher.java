/*
 * IdentityMatcher.java
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import freenet.pluginmanager.PluginNotFoundException;

public class IdentityMatcher {
	private final WoTConnection wotConnection;

	public IdentityMatcher(WoTConnection wotConnection) {
		this.wotConnection = wotConnection;
	}

	public Map<String, List<Identity>> matchIdentities(Set<String> recipients, String wotOwnIdentity, EnumSet<MatchMethod> methods) throws PluginNotFoundException {
		Set<Identity> wotIdentities = wotConnection.getAllTrustedIdentities(wotOwnIdentity);
		wotIdentities.addAll(wotConnection.getAllUntrustedIdentities(wotOwnIdentity));
		wotIdentities.addAll(wotConnection.getAllOwnIdentities());

		Map<String, List<Identity>> allMatches = new HashMap<String, List<Identity>>(recipients.size());

		for(String recipient : recipients) {
			allMatches.put(recipient, new LinkedList<Identity>());
		}

		for(Identity wotIdentity : wotIdentities) {
			for(String recipient : recipients) {
				if(matchIdentity(recipient, wotIdentity, methods)) {
					allMatches.get(recipient).add(wotIdentity);
				}
			}
		}

		return allMatches;
	}

	private boolean matchIdentity(String recipient, Identity wotIdentity, EnumSet<MatchMethod> methods) {
		for(MatchMethod method : methods) {
			switch(method) {
			case PARTIAL_BASE32:
				if(matchBase32Address(recipient, wotIdentity)) {
					return true;
				}
				break;
			case PARTIAL_BASE64:
				if(matchBase64Address(recipient, wotIdentity)) {
					return true;
				}
				break;
			}
		}

		return false;
	}

	private boolean matchBase64Address(String recipient, Identity identity) {
		String identityAddress = identity.getNickname() + "@" + identity.getIdentityID() + ".freemail";
		return identityAddress.startsWith(recipient);
	}

	private boolean matchBase32Address(String recipient, Identity identity) {
		String base32Id = identity.getBase32IdentityID();
		String identityAddress = identity.getNickname() + "@" + base32Id + ".freemail";

		//Change the recipient address to lower case, but leave the nickname in the original case
		if(recipient.contains("@")) {
			String recipientNickname = recipient.substring(0, recipient.indexOf("@"));
			String recipientDomain = recipient.substring(recipient.indexOf("@") + 1);
			recipient = recipientNickname + "@" + recipientDomain.toLowerCase();
		}

		return identityAddress.startsWith(recipient);
	}

	public enum MatchMethod {
		PARTIAL_BASE32,
		PARTIAL_BASE64;
	}
}
