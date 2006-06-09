package fnmail.imap;

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
		String retval = new String("");
		
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
