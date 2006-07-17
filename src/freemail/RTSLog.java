package freemail;

import java.util.Date;
import java.util.Set;
import java.util.Iterator;
import java.io.File;

import freemail.utils.PropsFile;
import freemail.utils.DateStringFactory;

public class RTSLog {
	PropsFile logfile;
	private static String NEXTID = "nextid-";
	private static String PASSES = "passes-";

	public RTSLog(File f) {
		this.logfile = new PropsFile(f);
	}
	
	public int getPasses(String day) {
		String val = this.logfile.get(PASSES+day);
		
		if (val == null)
			return 0;
		else
			return Integer.parseInt(val);
	}
	
	public void incPasses(String day) {
		this.logfile.put(PASSES+day, Integer.toString(this.getPasses(PASSES+day) + 1));
	}
	
	public void pruneBefore(Date keepafter) {
		Set props = this.logfile.listProps();
		
		Iterator i = props.iterator();
		while (i.hasNext()) {
			String cur = (String)i.next();
			
			String datestr;
			if (cur.startsWith(PASSES)) {
				datestr = cur.substring(PASSES.length());
			} else if (cur.startsWith(NEXTID)) {
				datestr = cur.substring(NEXTID.length());
			} else {
				continue;
			}
			
			Date logdate = DateStringFactory.DateFromKeyString(datestr);
			if (logdate == null) {
				// couldn't parse the date... hmm
				this.logfile.remove(cur);
			} else if (logdate.before(keepafter)) {
				this.logfile.remove(cur);
			}
		}
	}
	
	public int getNextId(String day) {
		String nid = this.logfile.get(NEXTID+day);
		if (nid == null) {
			return 1;
		} else {
			return Integer.parseInt(nid);
		}
	}
	
	public void incNextId(String day) {
		this.logfile.put(NEXTID+day, Integer.toString(this.getNextId(day) + 1));
	}
}
