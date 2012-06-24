/*
 * EmailAddress.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
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

package org.freenetproject.freemail.utils;


import org.archive.util.Base32;
import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.MailSite;

public class EmailAddress {
	private static final int SSK_PART_LENGTH = 43;

	public String realname;
	public String user;
	public String domain;

	public EmailAddress(String address) {
		if(address.matches("[^\\u0000-\\u007F]")) {
			throw new IllegalArgumentException("Address contains 8bit character");
		}
		if(address.matches("[\\u0000-\\u001F]")) {
			throw new IllegalArgumentException("Address contains ASCII control character");
		}
		if(address.matches("[\\u007F]")) {
			throw new IllegalArgumentException("Address contains ASCII DEL control character");
		}

		this.realname = null;
		this.user = null;
		this.domain = null;

		StringBuffer bank = new StringBuffer("");
		for (int i = 0; i < address.length(); i++) {
			char c = address.charAt(i);

			switch (c) {
				case '@':
					this.user = bank.toString().toLowerCase();
					bank = new StringBuffer("");
					break;
				case '<':
					this.realname = bank.toString();
					bank = new StringBuffer("");
					break;
				case '>':
					this.domain = bank.toString().toLowerCase();
					bank = new StringBuffer("");
					break;
				case '(':
					this.domain = bank.toString().toLowerCase();
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
			this.domain = bank.toString().toLowerCase();
		}

		// trim quotes out of the real name field
		if (realname != null) {
			this.realname = this.realname.trim();

			if((this.realname.length() > 0) && (this.realname.charAt(0) == '\"')) {
				this.realname = this.realname.substring(1);
			}

			if((this.realname.length() > 0) && (this.realname.charAt(this.realname.length() - 1) == '\"')) {
				this.realname = this.realname.substring(0, this.realname.length() - 1);
			}
		}
	}

	public boolean is_freemail_address() {
		if (this.domain == null) return false;
		if (!this.domain.endsWith(".freemail")) return false;
		return true;
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
	// note that the domain may contain additional dots, so we cannot use split
	public String getSubDomain() {
		int index=this.domain.lastIndexOf('.');
		if(index<0) {
			return null;
		} else {
			return this.domain.substring(0, index);
		}
	}

	public String getMailpageKey() {
		return "USK@"+new String (Base32.decode(this.getSubDomain()))+"/"+AccountManager.MAILSITE_SUFFIX+"/"+AccountManager.MAILSITE_VERSION+"/"+MailSite.MAILPAGE;
	}

	@Override
	public String toString() {
		return this.user+"@"+this.domain;
	}

	public String toLongString() {
		return this.realname + " <"+this.user+"@"+this.domain+">";
	}

	/**
	 * Removes illegal characters from localPart and returns the result
	 * @param localPart the local part that should be cleaned
	 * @return localPart without any illegal characters
	 */
	public static String cleanLocalPart(String localPart) {
		if(localPart == null) {
			throw new NullPointerException("Parameter localPart was null");
		}

		return localPart.replaceAll("[^A-Za-z0-9!#$%&'*+\\-/=?^_`{|}~]", "");
	}
}
