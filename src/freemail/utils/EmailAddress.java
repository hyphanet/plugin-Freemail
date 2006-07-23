package freemail.utils;

import org.archive.util.Base32;

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
	
	public EmailAddress() {
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
		
		return new String (Base32.decode(domparts[0]));
	}
	
	public String toString() {
		return this.user+"@"+this.domain;
	}
}
