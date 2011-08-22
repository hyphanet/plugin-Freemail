/*
 * FreemailL10n.java
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

package freemail.l10n;

import freemail.utils.Logger;
import freenet.l10n.PluginL10n;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.support.HTMLNode;

public class FreemailL10n {
	private static PluginL10n pluginL10n;

	public static String getString(String key) {
		return pluginL10n.getBase().getString(key);
	}

	public static String getString(String key, String patterns, String values) {
		return pluginL10n.getBase().getString(key, patterns, values);
	}

	public static String getString(String key, String[] patterns, String[] values) {
		return pluginL10n.getBase().getString(key, patterns, values);
	}

	public static void addL10nSubstitution(HTMLNode parent, String key, String[] patterns, HTMLNode[] values) {
		pluginL10n.getBase().addL10nSubstitution(parent, key, patterns, values);
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
		return "Freemail_lang_${lang}.override.l10n";
	}
}
