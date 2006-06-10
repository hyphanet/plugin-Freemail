package freemail;

import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;


class MailLog {
	private final File logfile;
	private HashMap messages;
	private int lastMessageId;
	private int passes;
	
	MailLog(File logfile) {
		this.lastMessageId = 0;
		this.passes = 0;
		
		this.messages = new HashMap();
		this.logfile = logfile;
		
		FileReader frdr;
		try {
			frdr = new FileReader(this.logfile);
		
		
			BufferedReader br = new BufferedReader(frdr);
			String line;
			
			while ( (line = br.readLine()) != null) {
				String[] parts = line.split("=");
				
				if (parts.length != 2) continue;
				
				if (parts[0].equalsIgnoreCase("passes")) {
					this.passes = Integer.parseInt(parts[1]);
					continue;
				}
				
				int thisnum = Integer.parseInt(parts[0]);
				if (thisnum > this.lastMessageId)
					this.lastMessageId = thisnum;
				this.messages.put(new Integer(thisnum), parts[1]);
			}
			
			frdr.close();
		} catch (IOException ioe) {
			return;
		}
	}
	
	public int incPasses() {
		this.passes++;
		this.writeLogFile();
		return this.passes;
	}
	
	public int getPasses() {
		return this.passes;
	}

	public int getNextMessageId() {
		return this.lastMessageId + 1;
	}
	
	public void addMessage(int num, String checksum) {
		this.messages.put(new Integer(num), checksum);
		if (num > this.lastMessageId)
			this.lastMessageId = num;
		this.writeLogFile();
	}
	
	private void writeLogFile() {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(this.logfile);
		} catch (IOException ioe) {
			return;
		}
		
		PrintWriter pw = new PrintWriter(fos);
		
		pw.println("passes="+this.passes);
		
		Iterator i = this.messages.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry)i.next();
			
			Integer num = (Integer)e.getKey();
			String checksum = (String)e.getValue();
			pw.println(num.toString()+"="+checksum);
		}
		
		pw.flush();
		
		try {
			fos.close();
		} catch (IOException ioe) {
			return;
		}
	}
}
