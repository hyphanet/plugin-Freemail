/*
 * IMAPMessageFlags.java
 * This file is part of Freemail, copyright (C) 2006 Dave Baker
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * 
 */

package freemail.imap;

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
	
	// these should be in the same order as the last so it's possible
	// to cross-reference
	public static final String[] allFlags = {
		"\\Seen",
		"\\Answered",
		"\\Flagged",
		"\\Deleted",
		"\\Draft",
		"\\Recent",
	};
	
	public static final String[] permanentFlags = {
		"\\Answered",
		"\\Flagged",
		"\\Deleted",
		"\\Draft",
		"\\Recent",
	};
	
	public static String getAllFlagsAsString() {
		int i;
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		
		for (i = 0; i < allFlags.length; i++) {
			if (!first)
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
		
		for (i = 0; i < permanentFlags.length; i++) {
			if (!first)
				buf.append(" ");
			first = false;
			buf.append(permanentFlags[i]);
		}
		
		return buf.toString();
	}

	private Vector flags;
	
	public IMAPMessageFlags() {
		this.flags = new Vector();
	}
	
	public IMAPMessageFlags(String shortflags) {
		this.flags = new Vector();
		for (int i = 0; i < allShortFlags.length; i++) {
			if (shortflags.indexOf(allShortFlags[i]) >= 0) {
				this.flags.add(allFlags[i]);
			}
		}
	}
	
	public void set(String flag, boolean value) {
		flag = sanitize_flag(flag);
		
		if (flag == null) return;
		
		if (value) {
			this.flags.add(flag);
		} else {
			this.flags.remove(flag);
		}
	}
	
	public String getShortFlagString() {
		String retval = new String("");
		
		for (int i = 0; i < allFlags.length; i++) {
			if (this.flags.contains(allFlags[i])) {
				retval += allShortFlags[i];
			}
		}
		
		return retval;
	}
	
	public String getFlags() {
		String retval = "";
		
		for (int i = 0; i < allFlags.length; i++) {
			if (this.flags.contains(allFlags[i])) {
				if (retval.length() > 0) retval += " ";
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
		
		if (flag == null) return false;
		
		if (this.flags.contains(flag)) return true;
		return false;
	}
	
	// take a flag, check it's real flag, and if so,
	// return it in the proper capitalisation
	private static String sanitize_flag(String flag) {
		String realFlag = null;
		
		for (int i = 0; i < allFlags.length; i++) {
			if (allFlags[i].toLowerCase().equals(flag.toLowerCase())) {
				realFlag = allFlags[i];
			}
		}
		return realFlag;
	}
}
