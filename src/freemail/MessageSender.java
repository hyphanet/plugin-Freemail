package freemail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;

import freemail.fcp.HighLevelFCPClient;
import freemail.fcp.FCPInsertErrorMessage;
import freemail.fcp.FCPBadFileException;
import freemail.utils.EmailAddress;
import freemail.utils.DateStringFactory;

public class MessageSender implements Runnable {
	public static final String OUTBOX_DIR = "outbox";
	public static final int MIN_RUN_TIME = 60000;
	public static final String NIM_KEY_PREFIX = "KSK@freemail-nim-";
	private final File datadir;
	private Thread senderthread;
	
	public MessageSender(File d) {
		this.datadir = d;
	}
	
	public void send_message(String from_user, Vector to, File msg) throws IOException {
		File user_dir = new File(this.datadir, from_user);
		File outbox = new File(user_dir, OUTBOX_DIR);
		
		Enumeration e = to.elements();
		while (e.hasMoreElements()) {
			EmailAddress email = (EmailAddress) e.nextElement();
			
			this.copyToOutbox(msg, outbox, email.user + "@" + email.domain);
		}
		this.senderthread.interrupt();
	}
	
	private synchronized void copyToOutbox(File src, File outbox, String to) throws IOException {
		File tempfile = File.createTempFile("fmail-msg-tmp", null, Freemail.getTempDir());
		
		FileOutputStream fos = new FileOutputStream(tempfile);
		FileInputStream fis = new FileInputStream(src);
		
		byte[] buf = new byte[1024];
		int read;
		while ( (read = fis.read(buf)) > 0) {
			fos.write(buf, 0, read);
		}
		fis.close();
		fos.close();
		
		File destfile;
		int prefix = 1;
		do {
			String filename = prefix + ":" + to;
			destfile = new File(outbox, filename);
			prefix++;
		} while (destfile.exists());
			
		tempfile.renameTo(destfile);
	}
	
	public void run() {
		this.senderthread = Thread.currentThread();
		while (true) {
			long start = System.currentTimeMillis();
			
			// iterate through users
			File[] files = this.datadir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().startsWith("."))
					continue;
				File outbox = new File(files[i], OUTBOX_DIR);
				if (!outbox.exists())
					outbox.mkdir();
				
				this.sendDir(outbox);
			}
			// don't spin around the loop if nothing's
			// going on
			long runtime = System.currentTimeMillis() - start;
			
			if (MIN_RUN_TIME - runtime > 0) {
				try {
					Thread.sleep(MIN_RUN_TIME - runtime);
				} catch (InterruptedException ie) {
				}
			}
		}
	}
	
	private void sendDir(File dir) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().startsWith("."))
				continue;
			
			this.sendSingle(files[i]);
		}
	}
	
	private void sendSingle(File msg) {
		String parts[] = msg.getName().split(":", 2);
		EmailAddress addr;
		if (parts.length < 2) {
			addr = new EmailAddress(parts[0]);
		} else {
			addr = new EmailAddress(parts[1]);
		}
		
		if (addr.domain == null || addr.domain.length() == 0) {
			msg.delete();
			return;
		}
		
		if (addr.domain.equalsIgnoreCase("nim.freemail")) {
			HighLevelFCPClient cli = new HighLevelFCPClient();
			
			FileInputStream fis;
			try {
				fis = new FileInputStream(msg);
			} catch (FileNotFoundException fnfe) {
				return;
			}
			
			if (cli.SlotInsert(fis, NIM_KEY_PREFIX+addr.user+"-"+DateStringFactory.getKeyString(), 1, "") > -1) {
				msg.delete();
			}
		}
	}
}
