/*
 * NIMContact.java
 * This file is part of Freemail, copyright (C) 2006,2008 Dave Baker
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

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Date;

import freemail.utils.DateStringFactory;

public class NIMContact {
	public static final String KEYFILE = "key";
	private static final String LOGFILE_PREFIX = "log-";
	private final File contact_dir;
	private final File keyfile;

	NIMContact(File dir) {
		this.contact_dir = dir;
		this.keyfile = new File(dir, KEYFILE);
	}
	
	public String getKey() throws IOException {
		FileReader frdr = new FileReader(this.keyfile);
		BufferedReader br = new BufferedReader(frdr);
		String key =  br.readLine();
		br.close();
		frdr.close();
		return key;
	}
	
	public MailLog getLog(String date) {
		return new MailLog(new File(this.contact_dir, LOGFILE_PREFIX + date));
	}
	
	public void pruneLogs(Date keepafter) {
		File[] files = contact_dir.listFiles();
		
		int i;
		for (i = 0; i< files.length; i++) {
			if (!files[i].getName().startsWith(LOGFILE_PREFIX))
				continue;
			Date logdate = DateStringFactory.dateFromKeyString(files[i].getName().substring(LOGFILE_PREFIX.length()));
			if (logdate == null) {
				// couldn't parse the date... hmm
				files[i].delete();
			} else if (logdate.before(keepafter)) {
				files[i].delete();
			}
		}
	}
}
