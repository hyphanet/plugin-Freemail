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

public class PropsFile {
	private final File file;
	private HashMap data;
	private BufferedReader bufrdr;

	/** Pass true into stopAtBlank to cause the reader to stop upon encountering
	 * a blank line. It's the the caller's responsibility to get
	 * (using the getReader() method) the stream and close it properly.
	 */
	public PropsFile(File f, boolean stopAtBlank) {
		this.file = f;
		this.data = null;
		
		if (f.exists()) {
			try {
				this.bufrdr = this.read(stopAtBlank);
			} catch (IOException ioe) {
			}
		}
	}
	
	public PropsFile(File f) {
		this(f, false);
	}
	
	private BufferedReader read(boolean stopAtBlank) throws IOException {
		this.data = new HashMap();
		
		BufferedReader br = new BufferedReader(new FileReader(this.file));
		
		String line = null;
		while ( (line = br.readLine()) != null) {
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
	
	private void write() throws IOException {
		PrintWriter pw = new PrintWriter(new FileOutputStream(this.file));
		
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
