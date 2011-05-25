package freemail.l10n;

import freemail.utils.Logger;
import freenet.l10n.BaseL10n.LANGUAGE;

public class FreemailL10n {
	public static String getString(String key) {
		Logger.error(FreemailL10n.class, "Missing translation for key " + key);
		return key;
	}

	public static void setLanguage(LANGUAGE newLanguage) {
		Logger.error(FreemailL10n.class, "Got new language " + newLanguage + ", but can't handle it");
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
