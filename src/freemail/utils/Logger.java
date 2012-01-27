/*
 * Logger.java
 * This file is part of Freemail
 * Copyright (C) 2007 Alexander Lehmann
 * Copyright (C) 2008 Dave Baker
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

package freemail.utils;

import java.lang.NoClassDefFoundError;
import java.text.SimpleDateFormat;
import java.util.Date;

import freemail.config.ConfigClient;
import freenet.support.Logger.LogLevel;

/**
 * Logger class for Freemail. This is a first attempt at fixing the logging so
 * that not everything is written to stdout. This class attempts to mimic the
 * Logger class from Freenet and calls the Freenet Logger class is available.
 */
public class Logger implements ConfigClient {
	private static final int INTERNAL = 1;
	private static final int DEBUG = 2;
	private static final int MINOR = 4;
	private static final int NORMAL = 8;
	private static final int ERROR = 16;

	static boolean initialized = false;
	static boolean foundFreenetLogger = false;

	// static final private int loglevel=INTERNAL|DEBUG|MINOR|NORMAL|ERROR; // everything
	// static final private int loglevel=DEBUG|NORMAL|ERROR;
	private static int loglevel = NORMAL | ERROR; // should be ok for normal users

	private static SimpleDateFormat logDateFormat = new SimpleDateFormat("d/MM/yyyy HH:mm:ss");

	@Override
	public void setConfigProp(String key, String val) {
		if(key.equals("loglevel")) {
			String[] levels = val.split("\\s*\\|\\s*");

			loglevel = 0;

			for (int i = 0; i < levels.length; i++) {
				if(levels[i].equalsIgnoreCase("internal")) {
					loglevel |= INTERNAL;
				} else if(levels[i].equalsIgnoreCase("debug")) {
					loglevel |= DEBUG;
				} else if(levels[i].equalsIgnoreCase("minor")) {
					loglevel |= MINOR;
				} else if(levels[i].equalsIgnoreCase("normal")) {
					loglevel |= NORMAL;
				} else if(levels[i].equalsIgnoreCase("error")) {
					loglevel |= ERROR;
				}
			}
		}
	}

	private static boolean useFreenetLogger() {
		if(!initialized) {
			try {
				freenet.support.Logger.shouldLog(LogLevel.ERROR, null);
				foundFreenetLogger = true;
			} catch (NoClassDefFoundError ex) {
				foundFreenetLogger = false;
			}
			initialized = true;
		}
		return foundFreenetLogger;
	}

	private static void log(int l, Object o, String s, String level) {
		log(l, o.getClass(), s, level);
	}

	private static synchronized void log(int l, Class<?> c, String s, String level) {
		if((l & loglevel) != 0) {
			System.err.println(logDateFormat.format(new Date()) + " " + level + "(" + c.getName() + "): " + s);
		}
	}

	public static void minor(Object o, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.minor(o, s);
		} else {
			log(MINOR, o, s, "MINOR");
		}
	}

	public static void minor(Class<?> c, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.minor(c, s);
		} else {
			log(MINOR, c, s, "MINOR");
		}
	}

	public static void normal(Object o, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.normal(o, s);
		} else {
			log(NORMAL, o, s, "NORMAL");
		}
	}

	public static void normal(Class<?> c, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.normal(c, s);
		} else {
			log(NORMAL, c, s, "NORMAL");
		}
	}

	public static void error(Object o, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.error(o, s);
		} else {
			log(ERROR, o, s, "ERROR");
		}
	}

	public static void error(Class<?> c, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.error(c, s);
		} else {
			log(ERROR, c, s, "ERROR");
		}
	}

	public static void debug(Object o, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.debug(o, s);
		} else {
			log(DEBUG, o, s, "DEBUG");
		}
	}

	public static void debug(Class<?> c, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.debug(c, s);
		} else {
			log(DEBUG, c, s, "DEBUG");
		}
	}
}
