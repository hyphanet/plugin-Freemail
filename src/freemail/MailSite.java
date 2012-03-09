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

package freemail;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import freemail.utils.PropsFile;
import freemail.fcp.FCPException;
import freemail.fcp.HighLevelFCPClient;
import freemail.fcp.FCPPutFailedException;
import freemail.fcp.FCPBadFileException;
import freemail.fcp.ConnectionTerminatedException;
import freemail.utils.Logger;

public class MailSite {
	private final PropsFile accprops;
	public static final String MAILPAGE = "mailpage";
	public static final String ALIAS_SUFFIX = "-mailsite";

	MailSite(PropsFile a) {
		this.accprops = a;
	}
	
	private String getMailPage() {
		StringBuffer buf = new StringBuffer();
		
		String rtsksk = this.accprops.get("rtskey");
		if (rtsksk == null) {
			Logger.error(this,"Can't insert mailsite - missing RTS KSK");
			return null;
		}
		buf.append("rtsksk=").append(rtsksk).append("\r\n");
		
		String keymodulus = this.accprops.get("asymkey.modulus");
		if (keymodulus == null) {
			Logger.error(this,"Can't insert mailsite - missing asymmetic crypto key modulus");
			return null;
		}
		buf.append("asymkey.modulus=").append(keymodulus).append("\r\n");
		
		String key_pubexponent = this.accprops.get("asymkey.pubexponent");
		if (key_pubexponent == null) {
			Logger.error(this,"Can't insert mailsite - missing asymmetic crypto key public exponent");
			return null;
		}
		buf.append("asymkey.pubexponent=").append(key_pubexponent).append("\r\n");
		
		return buf.toString();
	}
	
	public int publish(int minslot) throws InterruptedException {
		byte[] mailpage;
		String mailsite_s = this.getMailPage();
		if (mailsite_s == null) {
			return -1;
		}
		try {
			mailpage = mailsite_s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException use) {
			mailpage = mailsite_s.getBytes();
		}
		
		String key = this.accprops.get("mailsite.privkey");
		if (key == null) return -1;
		
		HighLevelFCPClient cli = new HighLevelFCPClient();
		
		int actualslot = -1;
		try {
			actualslot = cli.slotInsert(mailpage, key, minslot, "/"+MAILPAGE);
		} catch (ConnectionTerminatedException cte) {
			return -1;
		}
		
		if (actualslot < 0) return -1;
		
		this.accprops.put("mailsite.slot", new Integer(actualslot).toString());
		
		// leave this out for now, until we know whether we're doing it
		// with real USK redirects or fake put-a-USK-in-a-KSK redirect
		// are we set up to use a KSK domain alias too?
		String alias = this.accprops.get("domain_alias");
		if (alias != null) {
			if (!this.insertAlias(alias)) return -1;
		}
		
		return actualslot;
	}
	
	public boolean insertAlias(String alias) throws InterruptedException {
		String targetKey = this.accprops.get("mailsite.pubkey");
		if (targetKey == null) return false;
		FreenetURI furi;
		try {
			furi = new FreenetURI(targetKey);
		} catch (MalformedURLException mfue) {
			return false;
		}
		targetKey = "USK@"+furi.getKeyBody()+"/"+AccountManager.MAILSITE_SUFFIX+"/-1/"+MAILPAGE;
			
		ByteArrayInputStream bis = new ByteArrayInputStream(targetKey.getBytes());
			
		Logger.normal(this,"Inserting mailsite redirect from "+"KSK@"+alias+ALIAS_SUFFIX+" to "+targetKey);
		
		HighLevelFCPClient cli = new HighLevelFCPClient();
			
		FCPPutFailedException err = null;
		try {
			err = cli.put(bis, "KSK@"+alias+ALIAS_SUFFIX);
		} catch (FCPBadFileException bfe) {
				// impossible
			throw new AssertionError();
		} catch (ConnectionTerminatedException cte) {
			return false;
		} catch (FCPException e) {
			Logger.error(this, "Unknown error while inserting mailsite redirect: " + e);
			return false;
		}
			
		if (err == null) {
			Logger.normal(this,"Mailsite redirect inserted successfully");
			return true;
		} else if (err.errorcode == FCPPutFailedException.COLLISION) {
			Logger.error(this,"Mailsite alias collided - somebody is already using that alias! Choose another one!");
			return false;
		} else if (err.errorcode == FCPPutFailedException.REJECTED_OVERLOAD) {
			Logger.error(this,"Mailsite alias could not be inserted (rejected overload), this is probably a temporary error");
			return false;
		} else {
			Logger.error(this,"Mailsite redirect insert failed, but did not collide. (errorcode="+err.errorcode+")");
			return false;
		}
	}
}
