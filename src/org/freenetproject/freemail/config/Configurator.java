/*
 * Configurator.java
 * This file is part of Freemail
 * Copyright (C) 2006 Dave Baker
 * Copyright (C) 2008 Alexander Lehmann
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

package org.freenetproject.freemail.config;

import java.io.File;
import java.util.HashMap;

import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.utils.PropsFile;


/** "Probably the simplest config interface in the world"
 *
 */

public class Configurator {
	public static final String DATA_DIR = "datadir";
	public static final String FCP_HOST = "fcp_host";
	public static final String FCP_PORT = "fcp_port";
	public static final String GLOBAL_DATA_DIR = "globaldatadir";
	public static final String IMAP_BIND_ADDRESS = "imap_bind_address";
	public static final String IMAP_BIND_PORT = "imap_bind_port";
	public static final String LOG_LEVEL = "loglevel";
	public static final String SMTP_BIND_ADDRESS = "smtp_bind_address";
	public static final String SMTP_BIND_PORT = "smtp_bind_port";
	public static final String TEMP_DIR = "tempdir";
	public static final String FILE_FORMAT = "fileformat";

	private final PropsFile props;
	private final HashMap<String, ConfigClient> callbacks;

	public Configurator(File f) {
		this.props = PropsFile.createPropsFile(f);
		this.props.setCommentPrefix("#");
		String ls = System.getProperty("line.separator");
		StringBuffer head = new StringBuffer();
		head.append("# This is the configuration file for Freemail."+ls);
		head.append("# "+ls);
		head.append("# You are free to edit this file, but if Freemail"+ls);
		head.append("# breaks as a result, you should delete this file"+ls);
		head.append("# and Freemail will reset all values to their"+ls);
		head.append("# defaults."+ls);
		head.append("# The Freemail community cannot provide support"+ls);
		head.append("# to people who have broken Freemail as a result"+ls);
		head.append("# of editing this file.");

		this.props.setHeader(head.toString());
		this.callbacks = new HashMap<String, ConfigClient>();
	}

	/**
	 * Returns the value of the specified configuration key, or null if the key has not been set
	 * @param key the key whose value should be returned
	 * @return the value of the specified configuration key, or null if the key has not been set
	 */
	public String get(String key) {
		return props.get(key);
	}

	public void register(String key, ConfigClient cb, String defaultval) {
		ConfigClient old = this.callbacks.put(key, cb);
		if(old != null) {
			Logger.warning(this, "Replacing previous callback for " + key);
		}

		String val = this.props.get(key);
		if(val == null) {
			val = defaultval;
			props.put(key, val);
		}
		cb.setConfigProp(key, val);
	}

	public void set(String key, String val) {
		this.props.put(key, val);

		ConfigClient cb = this.callbacks.get(key);
		if(cb == null) return;
		cb.setConfigProp(key, val);
	}
}
