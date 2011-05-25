package freemail.l10n;

import freemail.utils.Logger;
import freenet.l10n.PluginL10n;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.pluginmanager.FredPluginBaseL10n;

public class FreemailL10n {
	private static PluginL10n pluginL10n;

	public static String getString(String key) {
		return pluginL10n.getBase().getString(key);
	}

	public static void setLanguage(FredPluginBaseL10n l10nBasePlugin, LANGUAGE newLanguage) {
		pluginL10n = new PluginL10n(l10nBasePlugin, newLanguage);
		Logger.debug(FreemailL10n.class, "Setting language to " + newLanguage);
	}

	public static String getL10nFilesBasePath() {
		return "freemail/l10n/";
	}

	public static String getL10nFilesMask() {
		return "lang_${lang}.l10n";
	}

	public static String getL10nOverrideFilesMask() {
		return "Freetalk_lang_${lang}.override.l10n";
	}
}
