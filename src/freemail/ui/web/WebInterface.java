package freemail.ui.web;

import java.util.HashSet;
import java.util.Set;

import freemail.utils.Logger;
import freenet.clients.http.PageMaker;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContainer;
import freenet.pluginmanager.FredPluginL10n;

public class WebInterface {
	private static final String FREEMAIL_CATEGORY_NAME = "Freemail.Menu.Name";
	private static final String CATEGORY_DEFAULT_PATH = "/Freemail/";
	private static final String CATEGORY_TITLE = "Freemail.Menu.Title";

	/**
	 * Holds all the Toadlets that must be unregistered when the web interface terminates
	 */
	private final Set<Toadlet> registeredToadlets = new HashSet<Toadlet>();
	private final ToadletContainer container;
	private final PageMaker pageMaker;

	public WebInterface(ToadletContainer container, PageMaker pageMaker, FredPluginL10n l10nPlugin) {
		this.container = container;
		this.pageMaker = pageMaker;

		//Register our menu
		pageMaker.addNavigationCategory(CATEGORY_DEFAULT_PATH, FREEMAIL_CATEGORY_NAME, CATEGORY_TITLE, l10nPlugin);

		//Register the visible toadlets
		HomeToadlet homeToadlet = new HomeToadlet(null);
		container.register(homeToadlet, FREEMAIL_CATEGORY_NAME, homeToadlet.path(), true, "Freemail.HomeToadlet.name", "Freemail.HomeToadlet.title", false, homeToadlet.getLinkEnabledCallback());
	}

	public void terminate() {
		Logger.error(this, "Unregistering toadlets");
		for(Toadlet t : registeredToadlets) {
			container.unregister(t);
		}

		Logger.error(this, "Removing navigation category");
		pageMaker.removeNavigationCategory(FREEMAIL_CATEGORY_NAME);
	}
}
