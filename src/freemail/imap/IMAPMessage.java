package freemail.imap;

import java.util.Vector;
import java.util.Stack;

public class IMAPMessage {
	public final String tag;
	public final String type;
	public final String[] args;

	IMAPMessage(String raw) throws IMAPBadMessageException {
		char[] a1 = {'[', '"'};
		char[] a2 = {']', '"'};
		
		String[] parts = doSplit(raw, a1, a2);
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
	
	public static String[] doSplit(String in, char c1, char c2) {
		char[] a1 = new char[1];
		a1[0] = c1;
		char[] a2 = new char[1];
		a2[0] = c2;
		return doSplit(in, a1, a2);
	}
	
	// split on spaces that aren't between two given characters
	public static String[] doSplit(String in, char[] c1, char[] c2) {
		Vector parts = new Vector();
		StringBuffer buf = new StringBuffer("");
		Stack context = new Stack();
		
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			
			int pos = -1;
			for (int j = 0; j < c1.length; j++) {
				if (c1[j] == c) {
					pos = j;
					break;
				}
			}
			
			if (!context.empty() && c == ((Character)context.peek()).charValue()) {
				context.pop();
				buf.append(c);
			} else if (pos >= 0) {
				context.push(new Character(c2[pos]));
				buf.append(c);
			} else if (c == ' ' && context.empty()) {
				parts.add(buf.toString());
				buf = new StringBuffer("");
			} else if (context.empty()) {
				buf.append(c);
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
