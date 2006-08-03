package freemail.utils;

import freemail.AccountManager;
import freemail.MailSite;

import org.archive.util.Base32;

public class EmailAddress {
	private static final int SSK_PART_LENGTH = 43;

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
		return true;
	}
	
	public boolean is_nim_address() {
		if (!this.is_freemail_address()) {
			return false;
		}
		return this.getSubDomain().equalsIgnoreCase("nim");
	}
	
	public boolean is_ssk_address() {
		if (!this.is_freemail_address()) return false;
		String key;
		try {
			key = new String(Base32.decode(this.getSubDomain()));
		} catch (Exception e) {
			return false;
		}
		
		String[] parts = key.split(",", 3);
		
		if (parts.length < 3) return false;
		if (parts[0].length() != SSK_PART_LENGTH || parts[1].length() != SSK_PART_LENGTH) return false;
		return true;
	}
	
	// get the part of the domain before the '.freemail'
	public String getSubDomain() {
		String[] domparts = this.domain.split("\\.", 2);
		
		if (domparts.length < 2) return null;
		
		return domparts[0];
	}
	
	public String getMailpageKey() {
		if (this.is_ssk_address()) {
			System.out.println("detected ssk address");
			
			return "USK@"+new String (Base32.decode(this.getSubDomain()))+"/"+AccountManager.MAILSITE_SUFFIX+"/"+AccountManager.MAILSITE_VERSION+"/"+MailSite.MAILPAGE;
		} else {
			System.out.println("detected ksk address");
			System.out.println("KSK@"+this.getSubDomain()+MailSite.ALIAS_SUFFIX);
			
			return "KSK@"+this.getSubDomain()+MailSite.ALIAS_SUFFIX;
		}
	}
	
	public String toString() {
		return this.user+"@"+this.domain;
	}
}
