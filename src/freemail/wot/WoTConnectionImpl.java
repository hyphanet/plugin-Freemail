/*
 * WoTConnectionImpl.java
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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import freemail.utils.SimpleFieldSetFactory;
import freenet.pluginmanager.FredPluginTalker;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.pluginmanager.PluginTalker;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

class WoTConnectionImpl implements WoTConnection {
	private static final String WOT_PLUGIN_NAME = "plugins.WebOfTrust.WebOfTrust";
	private static final String CONNECTION_IDENTIFIER = "Freemail";

	private final PluginTalker pluginTalker;

	private Message reply = null;
	private final Object replyLock = new Object();

	WoTConnectionImpl(PluginRespirator pr) throws PluginNotFoundException {
		pluginTalker = pr.getPluginTalker(new WoTConnectionTalker(), WOT_PLUGIN_NAME, CONNECTION_IDENTIFIER);
	}

	@Override
	public List<OwnIdentity> getAllOwnIdentities() {
		Message response = sendBlocking(
				new Message(
						new SimpleFieldSetFactory().put("Message", "GetOwnIdentities").create(),
						null));

		final List<OwnIdentity> ownIdentities = new LinkedList<OwnIdentity>();
		for(int count = 0;; count++) {
			String identityID = response.sfs.get("Identity" + count);
			if(identityID == null) {
				//Got all the identities
				break;
			}

			String requestURI = response.sfs.get("RequestURI" + count);
			String insertURI = response.sfs.get("InsertURI" + count);
			String nickname = response.sfs.get("Nickname" + count);

			ownIdentities.add(new OwnIdentity(identityID, requestURI, insertURI, nickname));
		}

		return ownIdentities;
	}

	@Override
	public Set<Identity> getAllTrustedIdentities(String trusterId) {
		return getAllIdentities(trusterId, TrustSelection.TRUSTED);
	}

	@Override
	public Set<Identity> getAllUntrustedIdentities(String trusterId) {
		return getAllIdentities(trusterId, TrustSelection.UNTRUSTED);
	}

	private Set<Identity> getAllIdentities(String trusterId, TrustSelection selection) {
		SimpleFieldSet sfs = new SimpleFieldSetFactory().create();
		sfs.putOverwrite("Message", "GetIdentitiesByScore");
		sfs.putOverwrite("Truster", trusterId);
		sfs.putOverwrite("Selection", selection.value);
		sfs.putOverwrite("Context", "");
		sfs.put("WantTrustValues", false);

		Message response = sendBlocking(new Message(sfs, null));

		final Set<Identity> identities = new HashSet<Identity>();
		for(int count = 0;; count++) {
			String identityID = response.sfs.get("Identity" + count);
			if(identityID == null) {
				//Got all the identities
				break;
			}

			String requestURI = response.sfs.get("RequestURI" + count);
			String nickname = response.sfs.get("Nickname" + count);

			identities.add(new Identity(identityID, requestURI, nickname));
		}

		return identities;
	}

	@Override
	public Identity getIdentity(String identity, String trusterId) {
		SimpleFieldSet sfs = new SimpleFieldSetFactory().create();
		sfs.putOverwrite("Message", "GetIdentity");
		sfs.putOverwrite("Identity", identity);
		sfs.putOverwrite("Truster", trusterId);

		Message response = sendBlocking(new Message(sfs, null));

		String requestURI = response.sfs.get("RequestURI");
		String nickname = response.sfs.get("Nickname");

		return new Identity(identity, requestURI, nickname);
	}

	private Message sendBlocking(final Message msg) {
		//Synchronize on pluginTalker so only one message can be sent at a time
		synchronized(pluginTalker) {
			pluginTalker.send(msg.sfs, msg.data);

			final Message retValue;
			synchronized(replyLock) {
				while(reply == null) {
					try {
						replyLock.wait();
					} catch (InterruptedException e) {
						//Just check again
					}
				}

				retValue = reply;
				reply = null;
			}

			return retValue;
		}
	}

	private static class Message {
		private final SimpleFieldSet sfs;
		private final Bucket data;

		private Message(SimpleFieldSet sfs, Bucket data) {
			this.sfs = sfs;
			this.data = data;
		}

		@Override
		public String toString() {
			return "[" + sfs + "] [" + data + "]";
		}
	}

	private class WoTConnectionTalker implements FredPluginTalker {
		@Override
		public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
			synchronized(replyLock) {
				assert reply == null : "Reply should be null, but was " + reply;

				reply = new Message(params, data);
				replyLock.notify();
			}
		}
	}

	private enum TrustSelection {
		TRUSTED("+"),
		ZERO("0"),
		UNTRUSTED("-");

		private final String value;
		private TrustSelection(String value) {
			this.value = value;
		}
	}
}
