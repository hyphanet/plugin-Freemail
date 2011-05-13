/*
 * RTSLog.java
 * This file is part of Freemail
 * Copyright (C) 2006,2008 Dave Baker
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

package freemail;

import java.util.Date;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;

import freemail.utils.Logger;
import freemail.utils.PropsFile;
import freemail.utils.DateStringFactory;

public class RTSLog {
	PropsFile logfile;
	private static String SLOTS = "slots-";
	private static String PASSES = "passes-";
	private static String UNPROC_NEXTID = "unproc-nextid";

	public RTSLog(File f) {
		this.logfile = PropsFile.createPropsFile(f);
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
		Date day = DateStringFactory.dateFromKeyString(daystr);
		String birth_s = this.logfile.get("birth");
		Date birth;
		if (birth_s == null) {
			birth = new Date();
			birth_s = DateStringFactory.getOffsetKeyString(0);
			this.logfile.put("birth", birth_s);
		} else {
			birth = DateStringFactory.dateFromKeyString(birth_s);
			if (birth.after(new Date())) {
				Logger.error(this, "RTS log was created in the future! Resetting to now");
				birth = new Date();
				birth_s = DateStringFactory.getOffsetKeyString(0);
				this.logfile.put("birth", birth_s);
			}
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
			
			Date logdate = DateStringFactory.dateFromKeyString(datestr);
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
