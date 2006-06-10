package freemail.imap;

import java.util.Vector;

public class IMAPMessage {
	public final String tag;
	public final String type;
	public final String[] args;

	IMAPMessage(String raw) throws IMAPBadMessageException {
		String[] parts = doSplit(raw, '[', ']');
		if (parts.length < 2) {
			throw new IMAPBadMessageException();
		}
		this.tag = parts[0];
		this.type = parts[1].toLowerCase();
		if (parts.length > 2) {
			this.args = new String[parts.length - 2];
			System.arraycopy(parts, 2, this.args, 0, parts.length - 2);
		} else {
			this.args = null;
		}
	}
	
	// split on spaces that aren't between two square given characters
	public static String[] doSplit(String in, char c1, char c2) {
		boolean in_brackets = false;
		Vector parts = new Vector();
		StringBuffer buf = new StringBuffer("");
		
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			
			if (c == c1) {
				in_brackets = true;
				buf.append(c);
			} else if (c == c2) {
				in_brackets = false;
				buf.append(c);
			} else if (c == ' ' && !in_brackets) {
				parts.add(buf.toString());
				buf = new StringBuffer("");
			} else buf.append(c);
		}
		
		parts.add(buf.toString());
		
		String[] retval = new String[parts.size()];
		
		for (int i = 0; i < parts.size(); i++) {
			retval[i] = (String)parts.get(i);
		}
		
		return retval;
	}
	
	// for debugging
	public String toString() {
		String retval = new String("");
		
		retval += this.tag + " ";
		retval += this.type;
		
		if (this.args == null) return retval;
		
		for (int i = 0; i < this.args.length; i++) {
			retval += " " + this.args[i];
		}
		return retval;
	}
}
