package freemail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;

import freemail.fcp.HighLevelFCPClient;
import freemail.utils.EmailAddress;
import freemail.utils.DateStringFactory;

public class MessageSender implements Runnable {
	public static final String OUTBOX_DIR = "outbox";
	private static final int MIN_RUN_TIME = 60000;
	public static final String NIM_KEY_PREFIX = "KSK@freemail-nim-";
	private static final int MAX_TRIES = 10;
	private final File datadir;
	private Thread senderthread;
	private static final String ATTR_SEP_CHAR = "_"; 
	
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
	
	private void copyToOutbox(File src, File outbox, String to) throws IOException {
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
		
		this.moveToOutbox(tempfile, 0, to, outbox);
	}
	
	// save a file to the outbox handling name collisions and atomicity
	private void moveToOutbox(File f, int tries, String to, File outbox) {
		File destfile;
		int prefix = 1;
		synchronized (this.senderthread) {
			do {
				String filename = prefix + ATTR_SEP_CHAR + tries + ATTR_SEP_CHAR + to;
				destfile = new File(outbox, filename);
				prefix++;
			} while (destfile.exists());
			
			f.renameTo(destfile);
		}
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
				
				this.sendDir(files[i], outbox);
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
	
	private void sendDir(File accdir, File dir) {
		File[] files = dir.listFiles();
		if (dir == null) return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().startsWith("."))
				continue;
			
			this.sendSingle(accdir, files[i]);
		}
	}
	
	private void sendSingle(File accdir, File msg) {
		String parts[] = msg.getName().split(ATTR_SEP_CHAR, 3);
		EmailAddress addr;
		int tries;
		if (parts.length < 3) {
			System.out.println("Warning invalid file in outbox - deleting.");
			msg.delete();
			return;
		} else {
			tries = Integer.parseInt(parts[1]);
			addr = new EmailAddress(parts[2]);
		}
		
		if (addr.domain == null || addr.domain.length() == 0) {
			msg.delete();
			return;
		}
		
		if (addr.is_nim_address()) {
			HighLevelFCPClient cli = new HighLevelFCPClient();
			
			if (cli.SlotInsert(msg, NIM_KEY_PREFIX+addr.user+"-"+DateStringFactory.getKeyString(), 1, "") > -1) {
				msg.delete();
			}
		} else {
			if (this.sendSecure(accdir, addr, msg)) {
				msg.delete();
			} else {
				tries++;
				if (tries > MAX_TRIES) {
					if (Postman.bounceMessage(msg, new MessageBank(accdir.getName()), "Tried too many times to deliver this message, but it doesn't apear that this address even exists. If you're sure that it does, check your Freenet connection.")) {
						msg.delete();
					}
				} else {
					this.moveToOutbox(msg, tries, parts[2], msg.getParentFile());
				}
			}
		}
	}
	
	private boolean sendSecure(File accdir, EmailAddress addr, File msg) {
		System.out.println("sending secure");
		OutboundContact ct;
		try {
			ct = new OutboundContact(accdir, addr);
		} catch (BadFreemailAddressException bfae) {
			// bounce
			return Postman.bounceMessage(msg, new MessageBank(accdir.getName()), "The address that this message was destined for ("+addr+") is not a valid Freemail address.");
		} catch (OutboundContactFatalException obfe) {
			// bounce
			return Postman.bounceMessage(msg, new MessageBank(accdir.getName()), obfe.getMessage());
		} catch (IOException ioe) {
			// couldn't get the mailsite - try again if you're not ready
			//to give up yet
			return false;
		}
		
		return ct.sendMessage(msg);
	}
}
