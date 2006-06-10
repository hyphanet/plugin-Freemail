package freemail;

import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.TreeMap;
import java.util.SortedMap;

public class MessageBank {
	private static final String MESSAGES_DIR = "inbox";
	private static final String NIDFILE = ".nextid";
	private static final String NIDTMPFILE = ".nextid-tmp";

	private final File dir;

	public MessageBank(String username) {
		this.dir = new File(AccountManager.DATADIR + File.separator + username + File.separator + MESSAGES_DIR);
		
		if (!this.dir.exists()) {
			this.dir.mkdir();
		}
	}
	
	public MailMessage createMessage() {
		long newid = this.nextId();
		File newfile;
		try {
			do {
				newfile = new File(this.dir, Long.toString(newid));
				newid++;
			} while (!newfile.createNewFile());
		} catch (IOException ioe) {
			newfile = null;
		}
		
		this.writeNextId(newid);
		
		if (newfile != null) {
			MailMessage newmsg = new MailMessage(newfile);
			return newmsg;
		}
		
		return null;
	}
	
	public SortedMap listMessages() {
		File[] files = this.dir.listFiles();
		
		TreeMap msgs = new TreeMap();
		
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().startsWith(".")) continue;
			
			MailMessage msg = new MailMessage(files[i]);
			
			msgs.put(new Integer(msg.getUID()), msg);
		}
		
		return msgs;
	}
	
	public MailMessage[] listMessagesArray() {
		File[] files = this.dir.listFiles(new MessageFileNameFilter());
		
		MailMessage[] msgs = new MailMessage[files.length];
		
		for (int i = 0; i < files.length; i++) {
			//if (files[i].getName().startsWith(".")) continue;
			
			MailMessage msg = new MailMessage(files[i]);
			
			msgs[i] = msg;
		}
		
		return msgs;
	}
	
	private long nextId() {
		File nidfile = new File(this.dir, NIDFILE);
		long retval;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(nidfile));
			
			retval = Long.parseLong(br.readLine());
		
			br.close();
		} catch (IOException ioe) {
			return 1;
		} catch (NumberFormatException nfe) {
			return 1;
		}
		
		return retval;
	}
	
	private void writeNextId(long newid) {
		// write the new ID to a temporary file
		File nidfile = new File(this.dir, NIDTMPFILE);
		try {
			PrintStream ps = new PrintStream(new FileOutputStream(nidfile));
			ps.print(newid);
			ps.flush();
			ps.close();
			
			// make sure the old nextid file doesn't contain a
			// value greater than our one
			if (this.nextId() <= newid) {
				nidfile.renameTo(new File(this.dir, NIDFILE));
			}
		} catch (IOException ioe) {
			// how to handle this?
		}
	}
	
	private class MessageFileNameFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			if (name.startsWith(".")) return false;
			return true;
		}
	}
}
