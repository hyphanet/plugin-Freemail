package freemail.utils;

import org.bouncycastle.util.encoders.Hex;

public class EmailAddress {
	public String realname;
	public String user;
	public String domain;
	
	public EmailAddress(String address) {
		this.realname = null;
		this.user = null;
		this.domain = null;
		
		StringBuffer bank = new StringBuffer("");
		for (int i = 0; i < address.length(); i++) {
			char c = address.charAt(i);
			
			switch (c) {
				case '@':
					this.user = bank.toString();
					bank = new StringBuffer("");
					break;
				case '<':
					this.realname = bank.toString();
					bank = new StringBuffer("");
					break;
				case '>':
					this.domain = bank.toString();
					bank = new StringBuffer("");
					break;
				case '(':
					this.domain = bank.toString();
					bank = new StringBuffer("");
					break;
				case ')':
					this.realname = bank.toString();
					bank = new StringBuffer("");
					break;
				default:
					bank.append(c);
			}
		}
		
		if (this.realname == null && this.domain == null) {
			this.domain = bank.toString();
		}
	}
	
	public boolean is_freemail_address() {
		if (this.domain == null) return false;
		if (!this.domain.endsWith(".freemail")) return false;
		if (this.getMailsiteKey() == null) return false;
		return true;
	}
	
	public String getMailsiteKey() {
		String[] domparts = this.domain.split("\\.", 2);
		
		if (domparts.length < 2) return null;
		
		try {
			return new String (Hex.decode(domparts[0].getBytes()));
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			// the Hex decoder just generates this exception if the input is not hex
			// (since it looks up a non-hex charecter in the decoding table)
			return null;
		}
	}
	
	public String toString() {
		return this.user+"@"+this.domain;
	}
}
