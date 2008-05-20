/*
 * PropsFile.java
 * This file is part of Freemail, copyright (C) 2006 Dave Baker
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * 
 */

package freemail.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Hashtable;

public class PropsFile {
	// substitute static methods for constructor
	
	static Hashtable propsList=new Hashtable();
	
	public static PropsFile createPropsFile(File f, boolean stopAtBlank) {
		String fn=f.getPath();

		PropsFile pf=(PropsFile)propsList.get(fn);
		
		if(pf!=null) {
			return pf;
		} else {
			pf=new PropsFile(f, stopAtBlank);
			propsList.put(fn, pf);
			return pf;
		}
	}

	public static PropsFile createPropsFile(File f) {
		return createPropsFile(f, false);
	}

	private final File file;
	private HashMap data;
	private BufferedReader bufrdr;
	private String commentPrefix;
	private String header;

	/** Pass true into stopAtBlank to cause the reader to stop upon encountering
	 * a blank line. It's the the caller's responsibility to get
	 * (using the getReader() method) the stream and close it properly.
	 */
	private PropsFile(File f, boolean stopAtBlank) {
		this.file = f;
		this.data = null;
		
		if (f.exists()) {
			try {
				this.bufrdr = this.read(stopAtBlank);
			} catch (IOException ioe) {
			}
		}
		this.commentPrefix = null;
		this.header = null;
	}
	
	public void setCommentPrefix(String cp) {
		this.commentPrefix = cp;
	}
	
	public void setHeader(String hdr) {
		this.header = hdr;
	}
	
	private synchronized BufferedReader read(boolean stopAtBlank) throws IOException {
		this.data = new HashMap();
		
		BufferedReader br = new BufferedReader(new FileReader(this.file));
		
		String line = null;
		while ( (line = br.readLine()) != null) {
			if (this.commentPrefix != null && line.startsWith(this.commentPrefix)) {
				continue;
			}
			if (stopAtBlank && line.length() == 0) {
				return br;
			}
			String[] parts = line.split("=", 2);
			if (parts.length < 2) continue;
			this.data.put(parts[0], parts[1]);
		}
		
		br.close();
		return null;
	}
	
	public BufferedReader getReader() {
		return this.bufrdr;
	}
	
	public void closeReader() {
		if (this.bufrdr == null) return;
		try {
			this.bufrdr.close();
		} catch (IOException ioe) {
		}
	}
	
	private synchronized void write() throws IOException {
		PrintWriter pw = new PrintWriter(new FileOutputStream(this.file));
		
		if (this.header != null) pw.println(this.header);
		
		Iterator i = this.data.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			String key = (String)e.getKey();
			String val = (String)e.getValue();
			
			pw.println(key+"="+val);
		}
		
		pw.close();
	}
	
	public String get(String key) {
		if (this.data == null) return null;
		
		return (String)this.data.get(key);
	}
	
	public boolean put(String key, String val) {
		if (this.data == null) {
			this.data = new HashMap();
		}
		
		this.data.put(key, val);
		try {
			this.write();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean put(String key, long val) {
		return this.put(key, Long.toString(val));
	}
	
	public boolean exists() {
		return this.file.exists();
	}
	
	public Set listProps() {
		return this.data.keySet();
	}
	
	public boolean remove(String key) {
		this.data.remove(key);
		try {
			this.write();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
		return true;
	}
}
