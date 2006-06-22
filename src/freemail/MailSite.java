package freemail;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import freemail.utils.PropsFile;
import freemail.fcp.HighLevelFCPClient;

public class MailSite {
	private final PropsFile accprops;
	public static final String MAILPAGE = "mailpage";

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
		
		ByteArrayInputStream bis = new ByteArrayInputStream(mailpage);
		
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
		
		int actualslot = cli.SlotInsert(bis, key, 1, "/"+MAILPAGE);
		
		this.accprops.put("mailsite.slot", new Integer(actualslot).toString());
		
		return actualslot;
	}
}
