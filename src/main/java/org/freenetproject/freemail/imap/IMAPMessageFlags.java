/*
 * IMAPMessageFlags.java
 * This file is part of Freemail
 * Copyright (C) 2006,2008 Dave Baker
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

package org.freenetproject.freemail.imap;

import java.util.Locale;
import java.util.Vector;

public class IMAPMessageFlags {
	public static final char[] allShortFlags = {
		'S',
		'A',
		'F',
		'X',
		'D',
		'R',
	};

	public static final String FLAG_SEEN = "\\Seen";
	public static final String FLAG_ANSWERED = "\\Answered";
	public static final String FLAG_FLAGGED = "\\Flagged";
	public static final String FLAG_DELETED = "\\Deleted";
	public static final String FLAG_DRAFT = "\\Draft";
	public static final String FLAG_RECENT = "\\Recent";

	// these should be in the same order as the last so it's possible
	// to cross-reference
	public static final String[] allFlags = {
			FLAG_SEEN,
			FLAG_ANSWERED,
			FLAG_FLAGGED,
			FLAG_DELETED,
			FLAG_DRAFT,
			FLAG_RECENT,
	};

	public static final String[] permanentFlags = {
			FLAG_SEEN,
			FLAG_ANSWERED,
			FLAG_FLAGGED,
			FLAG_DELETED,
			FLAG_DRAFT,
			FLAG_RECENT,
	};

	public static String getAllFlagsAsString() {
		int i;
		StringBuffer buf = new StringBuffer();
		boolean first = true;

		for(i = 0; i < allFlags.length; i++) {
			if(!first)
				buf.append(" ");
			first = false;
			buf.append(allFlags[i]);
		}

		return buf.toString();
	}

	public static String getPermanentFlagsAsString() {
		int i;
		StringBuffer buf = new StringBuffer();
		boolean first = true;

		for(i = 0; i < permanentFlags.length; i++) {
			if(!first)
				buf.append(" ");
			first = false;
			buf.append(permanentFlags[i]);
		}

		return buf.toString();
	}

	private Vector<String> flags;

	public IMAPMessageFlags() {
		this.flags = new Vector<String>();
	}

	public IMAPMessageFlags(String shortflags) {
		this.flags = new Vector<String>();
		for(int i = 0; i < allShortFlags.length; i++) {
			if(shortflags.indexOf(allShortFlags[i]) >= 0) {
				this.flags.add(allFlags[i]);
			}
		}
	}

	public void set(String flag, boolean value) {
		flag = sanitize_flag(flag);

		if(flag == null) return;

		if(value) {
			this.flags.add(flag);
		} else {
			this.flags.remove(flag);
		}
	}

	public String getShortFlagString() {
		String retval = new String("");

		for(int i = 0; i < allFlags.length; i++) {
			if(this.flags.contains(allFlags[i])) {
				retval += allShortFlags[i];
			}
		}

		return retval;
	}

	public String getFlags() {
		String retval = "";

		for(int i = 0; i < allFlags.length; i++) {
			if(this.flags.contains(allFlags[i])) {
				if(retval.length() > 0) retval += " ";
				retval += allFlags[i];
			}
		}

		return retval;
	}

	public void clear() {
		this.flags.clear();
	}

	public boolean get(String flag) {
		flag = sanitize_flag(flag);

		if(flag == null) return false;

		if(this.flags.contains(flag)) return true;
		return false;
	}

	// take a flag, check it's real flag, and if so,
	// return it in the proper capitalisation
	private static String sanitize_flag(String flag) {
		String realFlag = null;

		for(int i = 0; i < allFlags.length; i++) {
			if(allFlags[i].toLowerCase(Locale.ROOT).equals(flag.toLowerCase(Locale.ROOT))) {
				realFlag = allFlags[i];
			}
		}
		return realFlag;
	}

	public boolean isSeen() {
		return get(FLAG_SEEN);
	}

	public void setSeen() {
		set(FLAG_SEEN, true);
	}

	public boolean isDeleted() {
		return get(FLAG_DELETED);
	}

	public void setDeleted() {
		set(FLAG_DELETED, true);
	}

	public boolean isRecent() {
		return get(FLAG_RECENT);
	}

	public void setRecent() {
		set(FLAG_RECENT, true);
	}

	public void clearRecent() {
		set(FLAG_RECENT, false);
	}

}
