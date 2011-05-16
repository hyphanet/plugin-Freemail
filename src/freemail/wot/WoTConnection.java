package freemail.wot;

import java.util.LinkedList;
import java.util.List;

import freemail.utils.SimpleFieldSetFactory;
import freenet.pluginmanager.FredPluginTalker;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.pluginmanager.PluginTalker;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

public class WoTConnection implements FredPluginTalker {
	private static final String WOT_PLUGIN_NAME = "plugins.WebOfTrust.WebOfTrust";
	private static final String CONNECTION_IDENTIFIER = "Freemail";

	private final PluginTalker pluginTalker;

	private Message reply = null;
	private final Object replyLock = new Object();

	public WoTConnection(PluginRespirator pr) throws PluginNotFoundException {
		pluginTalker = pr.getPluginTalker(this, WOT_PLUGIN_NAME, CONNECTION_IDENTIFIER);
	}

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
	public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
		synchronized(replyLock) {
			assert reply == null : "Reply should be null, but was " + reply;

			reply = new Message(params, data);
			replyLock.notify();
		}
	}

	private Message sendBlocking(final Message msg) {
		//Synchronize on pluginTalker so once on message can be sent at a time
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
}
