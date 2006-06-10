package freemail;

import java.io.File;
import java.io.IOException;

import freemail.fcp.FCPContext;
import freemail.fcp.FCPConnection;
import freemail.imap.IMAPListener;
import freemail.smtp.SMTPListener;

public class Freemail {
	private static final String TEMPDIRNAME = "temp";
	private static File datadir;
	private static File tempdir;
	private static FCPConnection fcpconn;
	
	public static File getTempDir() {
		return Freemail.tempdir;
	}
	
	public static FCPConnection getFCPConnection() {
		return Freemail.fcpconn;
	}

	public static void main(String[] args) {
		String fcphost = "localhost";
		int fcpport = 9481;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--newaccount")) {
				i++;
				if (args.length - 1 < i) {
					System.out.println("Usage: --newaccount <account name>");
					return;
				}
				try {
					AccountManager.Create(args[i]);
					// for now
					AccountManager.setupNIM(args[i]);
					System.out.println("Account created for "+args[i]+". You may now set a password with --passwd <password>");
					System.out.println("For the time being, you address is "+args[i]+"@nim.freemail");
				} catch (IOException ioe) {
					System.out.println("Couldn't create account. Please check write access to Freemail's working directory. Error: "+ioe.getMessage());
				}
				return;
			} else if (args[i].equals("--passwd")) {
				i = i + 2;
				if (args.length - 1 < i) {
					System.out.println("Usage: --passwd <account name> <password>");
					return;
				}
				try {
					AccountManager.ChangePassword(args[i - 1], args[i]);
					System.out.println("Password changed.");
				} catch (Exception e) {
					System.out.println("Couldn't change password for "+args[i - 1]+". "+e.getMessage());
				}
				return;
			} else if (args[i].equals("-h")) {
				i++;
				if (args.length - 1 < i) {
					System.out.println("No hostname supplied, using default");
					continue;
				}
				fcphost = args[i];
			} else if (args[i].equals("-p")) {
				i++;
				if (args.length - 1 < i) {
					System.out.println("No port supplied, using default");
					continue;
				}
				try {
					fcpport = Integer.parseInt(args[i]);
				} catch (NumberFormatException nfe) {
					System.out.println("Bad port supplied, using default");
				}
			}
		}
		
		FCPContext fcpctx = new FCPContext(fcphost, fcpport);
		
		Freemail.fcpconn = new FCPConnection(fcpctx);
		Thread fcpthread  = new Thread(fcpconn);
		fcpthread.setDaemon(true);
		fcpthread.start();
		
		// start a SingleAccountWatcher for each account
		Freemail.datadir = new File("data");
		if (!Freemail.datadir.exists()) {
			System.out.println("Starting Freemail for the first time.");
			System.out.println("You will probably want to add an account by running Freemail with arguments --newaccount <username>");
			if (!Freemail.datadir.mkdir()) {
				System.out.println("Couldn't create data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				System.exit(1);
			}
		}
		Freemail.tempdir = new File(Freemail.TEMPDIRNAME);
		if (!Freemail.tempdir.exists()) {
			if (!Freemail.tempdir.mkdir()) {
				System.out.println("Couldn't create temporary directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				System.exit(1);
			}
		}
		
		File[] files = Freemail.datadir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(".") || files[i].getName().equals(".."))
				continue;
			
			Thread t = new Thread(new SingleAccountWatcher(files[i]));
			t.setDaemon(true);
			t.start();
		}
		
		// and a sender thread
		MessageSender sender = new MessageSender(Freemail.datadir);
		Thread senderthread = new Thread(sender);
		senderthread.setDaemon(true);
		senderthread.start();
		
		// start the SMTP Listener
		SMTPListener smtpl = new SMTPListener(sender);
		Thread smtpthread = new Thread(smtpl);
		smtpthread.setDaemon(true);
		smtpthread.start();
		
		// start the IMAP listener
		IMAPListener imapl = new IMAPListener();
		imapl.run();
	}
}
