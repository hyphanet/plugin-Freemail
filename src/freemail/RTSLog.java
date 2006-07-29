package freemail;

import java.util.Date;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;

import freemail.utils.PropsFile;
import freemail.utils.DateStringFactory;

public class RTSLog {
	PropsFile logfile;
	private static String SLOTS = "slots-";
	private static String PASSES = "passes-";
	private static String UNPROC_NEXTID = "unproc-nextid";

	public RTSLog(File f) {
		this.logfile = new PropsFile(f);
		if (!this.logfile.exists()) {
			String birth = DateStringFactory.getOffsetKeyString(0);
			this.logfile.put("birth", birth);
		}
	}
	
	public int getPasses(String day) {
		if (this.isBeforeBirth(day)) return Integer.MAX_VALUE;
		
		String val = this.logfile.get(PASSES+day);
		
		if (val == null)
			return 0;
		else
			return Integer.parseInt(val);
	}
	
	private boolean isBeforeBirth(String daystr) {
		Date day = DateStringFactory.DateFromKeyString(daystr);
		String birth_s = this.logfile.get("birth");
		Date birth;
		if (birth_s == null) {
			birth = new Date();
			birth_s = DateStringFactory.getOffsetKeyString(0);
			this.logfile.put("birth", birth_s);
		} else {
			birth = DateStringFactory.DateFromKeyString(birth_s);
		}
		
		if (day.before(birth)) return true;
		return false;
	}
	
	public void incPasses(String day) {
		int passes = this.getPasses(day);
		passes++;
		
		this.logfile.put(PASSES+day, Integer.toString(passes));
	}
	
	public void pruneBefore(Date keepafter) {
		Set props = this.logfile.listProps();
		Vector hitlist = new Vector();
		
		Iterator i = props.iterator();
		while (i.hasNext()) {
			String cur = (String)i.next();
			
			String datestr;
			if (cur.startsWith(PASSES)) {
				datestr = cur.substring(PASSES.length());
			} else if (cur.startsWith(SLOTS)) {
				datestr = cur.substring(SLOTS.length());
			} else {
				continue;
			}
			
			Date logdate = DateStringFactory.DateFromKeyString(datestr);
			if (logdate == null) {
				// couldn't parse the date... hmm
				hitlist.add(cur);
			} else if (logdate.before(keepafter)) {
				hitlist.add(cur);
			}
		}
		
		Enumeration e = hitlist.elements();
		while (e.hasMoreElements()) {
			String victim = (String) e.nextElement();
			
			this.logfile.remove(victim);
		}
	}
	
	public String getSlots(String day) {
		String slots = this.logfile.get(SLOTS+day);
		if (slots == null) {
			return "1";
		} else {
			return slots;
		}
	}
	
	public void putSlots(String day, String slots) {
		this.logfile.put(SLOTS+day, slots);
	}
	
	public int getAndIncUnprocNextId() {
		String nid = this.logfile.get(UNPROC_NEXTID);
		int retval;
		if (nid == null) {
			retval = 1;
		} else {
			retval = Integer.parseInt(nid);
		}
		
		this.logfile.put(UNPROC_NEXTID, Integer.toString(retval + 1));
		
		return retval;
	}
}
