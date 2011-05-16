package freemail.wot;

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

	public WoTConnection(PluginRespirator pr) throws PluginNotFoundException {
		pluginTalker = pr.getPluginTalker(this, WOT_PLUGIN_NAME, CONNECTION_IDENTIFIER);
	}

	@Override
	public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
		assert(false); //Unsupported for now
	}
}
