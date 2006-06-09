package fnmail;

import java.io.File;
import java.io.IOException;

import fnmail.fcp.FCPContext;
import fnmail.fcp.FCPConnection;
import fnmail.imap.IMAPListener;
import fnmail.smtp.SMTPListener;

public class FNMail {
	private static final String TEMPDIRNAME = "temp";
	private static File datadir;
	private static File tempdir;
	private static FCPConnection fcpconn;
	
	public static File getTempDir() {
		return FNMail.tempdir;
	}
	
	public static FCPConnection getFCPConnection() {
		return FNMail.fcpconn;
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
					System.out.println("For the time being, you address is "+args[i]+"@nim.fnmail");
				} catch (IOException ioe) {
					System.out.println("Couldn't create account. Please check write access to fnmail's working directory. Error: "+ioe.getMessage());
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
		
		FNMail.fcpconn = new FCPConnection(fcpctx);
		Thread fcpthread  = new Thread(fcpconn);
		fcpthread.setDaemon(true);
		fcpthread.start();
		
		// start a SingleAccountWatcher for each account
		FNMail.datadir = new File("data");
		if (!FNMail.datadir.exists()) {
			System.out.println("Starting fnmail for the first time.");
			System.out.println("You will probably want to add an account by running fnmail with arguments --newaccount <username>");
			if (!FNMail.datadir.mkdir()) {
				System.out.println("Couldn't create data directory. Please ensure that the user you are running fnmail as has write access to its working directory");
				System.exit(1);
			}
		}
		FNMail.tempdir = new File(FNMail.TEMPDIRNAME);
		if (!FNMail.tempdir.exists()) {
			if (!FNMail.tempdir.mkdir()) {
				System.out.println("Couldn't create temporary directory. Please ensure that the user you are running fnmail as has write access to its working directory");
				System.exit(1);
			}
		}
		
		File[] files = FNMail.datadir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(".") || files[i].getName().equals(".."))
				continue;
			
			Thread t = new Thread(new SingleAccountWatcher(files[i]));
			t.setDaemon(true);
			t.start();
		}
		
		// and a sender thread
		MessageSender sender = new MessageSender(FNMail.datadir);
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
