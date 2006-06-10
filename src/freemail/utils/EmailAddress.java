package freemail.utils;

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
		return this.domain.equalsIgnoreCase("nim.freemail");
	}
}
