package freemail;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;

import freemail.imap.IMAPMessageFlags;

public class MailMessage {
	private File file;
	private OutputStream os;
	private PrintStream ps;
	private final Vector headers;
	private BufferedReader brdr;
	public final IMAPMessageFlags flags;
	
	MailMessage(File f) {
		this.file = f;
		this.headers = new Vector();
		
		// initalise flags from filename
		String[] parts = f.getName().split(",");
		if (parts.length < 2 && !f.getName().endsWith(",")) {
			// treat it as a new message
			this.flags = new IMAPMessageFlags();
			this.flags.set("\\Recent", true);
		} else if (parts.length < 2) {
			// just doesn't have any flags set
			this.flags = new IMAPMessageFlags();
		} else {
			this.flags = new IMAPMessageFlags(parts[1]);
		}
		this.brdr = null;
	}
	
	public void addHeader(String name, String val) {
		this.headers.add(new MailMessageHeader(name, val));
	}
	
	// get the first header of a given name
	public String getFirstHeader(String name) {
		Enumeration e = this.headers.elements();
		
		while (e.hasMoreElements()) {
			MailMessageHeader h = (MailMessageHeader) e.nextElement();
			
			if (h.name.equalsIgnoreCase(name)) {
				return h.val;
			}
		}
		
		return null;
	}
	
	public String getHeaders(String name) {
		StringBuffer buf = new StringBuffer("");
		
		Enumeration e = this.headers.elements();
		
		while (e.hasMoreElements()) {
			MailMessageHeader h = (MailMessageHeader) e.nextElement();
			
			if (h.name.equalsIgnoreCase(name)) {
				buf.append(h.name);
				buf.append(": ");
				buf.append(h.val);
				buf.append("\r\n");
			}
		}
		
		return buf.toString();
	}
	
	public PrintStream writeHeadersAndGetStream() throws FileNotFoundException {
		this.os = new FileOutputStream(this.file);
		this.ps = new PrintStream(this.os);
		
		Enumeration e = this.headers.elements();
		
		while (e.hasMoreElements()) {
			MailMessageHeader h = (MailMessageHeader) e.nextElement();
			
			this.ps.println(h.name + ": " + h.val);
		}
		
		this.ps.println("");
		
		return this.ps;
	}
	
	public void commit() {
		try {
			this.os.close();
			// also potentally move from a temp dir to real inbox
			// to do safer inbox access
		} catch (IOException ioe) {
			
		}
	}
	
	public void cancel() {
		try {
			this.os.close();
		} catch (IOException ioe) {
		}
		this.file.delete();
	}
	
	public void readHeaders() throws IOException {
		BufferedReader bufrdr = new BufferedReader(new FileReader(this.file));
		
		this.readHeaders(bufrdr);
		bufrdr.close();
	}
	
	public void readHeaders(BufferedReader bufrdr) throws IOException {
		String line;
		String[] parts = null;
		while ( (line = bufrdr.readLine()) != null) {
			if (line.length() == 0) {
				if (parts != null)
					this.addHeader(parts[0], parts[1]);
				parts = null;
				break;
			} else if (line.startsWith(" ")) {
				// contination of previous line
				if (parts == null || parts[1] == null) 
					continue;
				parts[1] += line.trim();
			} else {
				if (parts != null)
					this.addHeader(parts[0], parts[1]);
				parts = null;
				parts = line.split(": ", 2);
				
				if (parts.length < 2)
					parts = null;
			}
		}
		
		if (parts != null) {
			this.addHeader(parts[0], parts[1]);
		}
	}
	
	public int getUID() {
		String[] parts = this.file.getName().split(",");
		
		return Integer.parseInt(parts[0]);
	}
	
	public long getSize() throws IOException {
		// this is quite arduous since we have to send the message
		// with \r\n's, and hence it may not be the size it is on disk
		BufferedReader br = new BufferedReader(new FileReader(this.file));
		
		long counter = 0;
		String line;
		
		while ( (line = br.readLine()) != null) {
			counter += line.length();
			counter += "\r\n".length();
		}
		
		br.close();
		return counter;
	}
	
	public void closeStream() {
		try {
			if (this.brdr != null) this.brdr.close();
		} catch (IOException ioe) {
			
		}
		this.brdr = null;
	}
	
	public String readLine() throws IOException {
		if (this.brdr == null) {
			this.brdr = new BufferedReader(new FileReader(this.file));
		}
		
		return this.brdr.readLine();
	}
	
	// programming-by-contract - anything that tries to read the message
	// or suchlike after calling this method is responsible for the
	// torrent of exceptions they'll get thrown at them!
	public void delete() {
		this.file.delete();
	}
	
	public void storeFlags() {
		String[] parts = this.file.getName().split(",");
		
		String newname = parts[0] + "," + this.flags.getShortFlagString();
		
		this.file.renameTo(new File(this.file.getParentFile(), newname));
	}
	
	private class MailMessageHeader {
		public String name;
		public String val;
		
		public MailMessageHeader(String n, String v) {
			this.name = n;
			this.val = v;
		}
	}
}
