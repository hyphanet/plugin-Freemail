/*
 * MailSite.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007,2008 Alexander Lehmann
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

package org.freenetproject.freemail;

import java.io.UnsupportedEncodingException;

import org.freenetproject.freemail.fcp.ConnectionTerminatedException;
import org.freenetproject.freemail.fcp.HighLevelFCPClient;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.utils.PropsFile;


public class MailSite {
	private final PropsFile accprops;
	public static final String MAILPAGE = "mailpage";

	MailSite(PropsFile a) {
		this.accprops = a;
	}

	private String getMailPage() {
		StringBuffer buf = new StringBuffer();

		String rtsksk = this.accprops.get("rtskey");
		if(rtsksk == null) {
			Logger.error(this, "Can't insert mailsite - missing RTS KSK");
			return null;
		}
		buf.append("rtsksk=").append(rtsksk).append("\r\n");

		String keymodulus = this.accprops.get("asymkey.modulus");
		if(keymodulus == null) {
			Logger.error(this, "Can't insert mailsite - missing asymmetric crypto key modulus");
			return null;
		}
		buf.append("asymkey.modulus=").append(keymodulus).append("\r\n");

		String key_pubexponent = this.accprops.get("asymkey.pubexponent");
		if(key_pubexponent == null) {
			Logger.error(this, "Can't insert mailsite - missing asymmetric crypto key public exponent");
			return null;
		}
		buf.append("asymkey.pubexponent=").append(key_pubexponent).append("\r\n");

		return buf.toString();
	}

	public int publish(int minslot) throws InterruptedException {
		byte[] mailpage;
		String mailsite_s = this.getMailPage();
		if(mailsite_s == null) {
			return -1;
		}
		try {
			mailpage = mailsite_s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException use) {
			mailpage = mailsite_s.getBytes();
		}

		String key = this.accprops.get("mailsite.privkey");
		if(key == null) return -1;

		HighLevelFCPClient cli = new HighLevelFCPClient();

		int actualslot = -1;
		try {
			actualslot = cli.slotInsert(mailpage, key, minslot, "/"+MAILPAGE);
		} catch (ConnectionTerminatedException cte) {
			return -1;
		}

		if(actualslot < 0) return -1;

		this.accprops.put("mailsite.slot", new Integer(actualslot).toString());

		return actualslot;
	}
}
