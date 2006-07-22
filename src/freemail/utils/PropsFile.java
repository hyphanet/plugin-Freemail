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

	public PropsFile(File f) {
		this.file = f;
		this.data = null;
		
		if (f.exists()) {
			try {
				this.read();
			} catch (IOException ioe) {
			}
		}
	}
	
	private void read() throws IOException {
		this.data = new HashMap();
		
		BufferedReader br = new BufferedReader(new FileReader(this.file));
		
		String line = null;
		while ( (line = br.readLine()) != null) {
			String[] parts = line.split("=", 2);
			if (parts.length < 2) continue;
			this.data.put(parts[0], parts[1]);
		}
		
		br.close();
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
	
	public boolean exists() {
		return this.file.exists();
	}
	
	public Set listProps() {
		return this.data.keySet();
	}
	
	public void remove(String key) {
		this.data.remove(key);
	}
}
