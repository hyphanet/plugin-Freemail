package freemail;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import freemail.utils.PropsFile;
import freemail.fcp.HighLevelFCPClient;
import freemail.fcp.FCPInsertErrorMessage;

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
			System.out.println("Can't insert mailsite - missing RTS KSK");
			return null;
		}
		buf.append("rtsksk=").append(rtsksk).append("\r\n");
		
		String keymodulus = this.accprops.get("asymkey.modulus");
		if (keymodulus == null) {
			System.out.println("Can't insert mailsite - missing asymmetic crypto key modulus");
			return null;
		}
		buf.append("asymkey.modulus=").append(keymodulus).append("\r\n");
		
		String key_pubexponent = this.accprops.get("asymkey.pubexponent");
		if (key_pubexponent == null) {
			System.out.println("Can't insert mailsite - missing asymmetic crypto key public exponent");
			return null;
		}
		buf.append("asymkey.pubexponent=").append(key_pubexponent).append("\r\n");
		
		return buf.toString();
	}
	
	public int Publish() {
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
		
		String minslot_s = this.accprops.get("mailsite.slot");
		int minslot;
		if (minslot_s != null) {
			minslot = Integer.parseInt(minslot_s);
		} else {
			minslot = 1;
		}
		
		int actualslot = cli.SlotInsert(mailpage, key, minslot, "/"+MAILPAGE);
		
		if (actualslot < 0) return -1;
		
		this.accprops.put("mailsite.slot", new Integer(actualslot).toString());
		
		// leave this out for now, until we know whether we're doing it
		// with real USK redirects or fake put-a-USK-in-a-KSK redirect
		// are we set up to use a KSK domain alias too?
		/*String alias = this.accprops.get("domain_alias");
		if (alias != null) {
			String targetKey = this.accprops.get("mailsite.pubkey");
			if (targetKey == null) return -1;
			FreenetURI furi;
			try {
				furi = new FreenetURI(targetKey);
			} catch (MalformedURLException mfue) {
				return -1;
			}
			targetKey = "USK@"+furi.getKeyBody()+"/"+AccountManager.MAILSITE_SUFFIX+"/-1/"+MAILPAGE;
			
			System.out.println("Inserting mailsite redirect from "+"KSK@"+alias+ALIAS_SUFFIX+" to "+targetKey);
			
			FCPInsertErrorMessage err = cli.putRedirect("KSK@"+alias+ALIAS_SUFFIX, targetKey);
			
			if (err == null) {
				System.out.println("Mailsite redirect inserted successfully");
			} else if (err.errorcode == FCPInsertErrorMessage.COLLISION) {
				System.out.println("Mailsite alias collided - somebody is already using that alias! Choose another one!");
			} else {
				System.out.println("Mailsite redirect insert failed, but did not collide.");
			}
		}*/
		
		return actualslot;
	}
}
