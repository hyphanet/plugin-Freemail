package freemail;

import java.io.File;
import java.io.IOException;

import freemail.fcp.FCPContext;
import freemail.fcp.FCPConnection;
import freemail.imap.IMAPListener;
import freemail.smtp.SMTPListener;

public class Freemail {
	// version info
	public static final int VER_MAJOR = 0;
	public static final int VER_MINOR = 1;
	public static final int BUILD_NO = 6;
	public static final String VERSION_TAG = "Pet Shop";

	private static final String TEMPDIRNAME = "temp";
	private static final String DATADIR = "data";
	private static final String GLOBALDATADIR = "globaldata";
	private static final String ACKDIR = "delayedacks";
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
		
		String action = "";
		String account = null;
		String newpasswd = null;
		String alias = null;
		
		System.out.println("This is Freemail version "+VER_MAJOR+"."+VER_MINOR+" build #"+BUILD_NO+" ("+VERSION_TAG+")");
		System.out.println("Freemail is released under the terms of the GNU Lesser General Public License. Freemail is provided WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. For details, see the LICENSE file included with this distribution.");
		System.out.println("");
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--newaccount")) {
				action = args[i];
				i++;
				if (args.length - 1 < i) {
					System.out.println("Usage: --newaccount <account name>");
					return;
				}
				
				account = args[i];
			} else if (args[i].equals("--passwd")) {
				action = args[i];
				i = i + 2;
				if (args.length - 1 < i) {
					System.out.println("Usage: --passwd <account name> <password>");
					return;
				}
				account = args[i - 1];
				newpasswd = args[i];
			} else if (args[i].equals("--shortaddress")) {
				action = args[i];
				i = i + 2;
				if (args.length - 1 < i) {
					System.out.println("Usage: --shortaddress <name> <domain prefix>");
					return;
				}
				account = args[i - 1];
				alias = args[i];
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
		
		if (action.equals("--newaccount")) {
			try {
				AccountManager.Create(account);
				// by default, we'll not setup NIM now real mode works
				//AccountManager.setupNIM(account);
				System.out.println("Account created for "+account+". You may now set a password with --passwd <password>");
				//System.out.println("For the time being, you address is "+account+"@nim.freemail");
			} catch (IOException ioe) {
				System.out.println("Couldn't create account. Please check write access to Freemail's working directory. If you want to overwrite your account, delete the appropriate directory manually in 'data' first. Freemail will intentionally not overwrite it. Error: "+ioe.getMessage());
			}
			return;
		} else if (action.equals("--passwd")) {
			try {
				AccountManager.ChangePassword(account, newpasswd);
				System.out.println("Password changed.");
			} catch (Exception e) {
				System.out.println("Couldn't change password for "+account+". "+e.getMessage());
				e.printStackTrace();
			}
			return;
		} else if (action.equals("--shortaddress")) {
			try {
				AccountManager.addShortAddress(account, alias);
			} catch (Exception e) {
				System.out.println("Couldn't add short address for "+account+". "+e.getMessage());
				e.printStackTrace();
				return;
			}
			System.out.println("Your short Freemail address is: 'anything@"+alias+".freemail'. Your long address will continue to work.");
			return;
		}
		
		File globaldatadir = new File(GLOBALDATADIR);
		if (!globaldatadir.exists()) {
			globaldatadir.mkdir();
		}
		
		// start a SingleAccountWatcher for each account
		Freemail.datadir = new File(DATADIR);
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
			if (!files[i].isDirectory()) continue;
			
			Thread t = new Thread(new SingleAccountWatcher(files[i]), "Account Watcher for "+files[i].getName());
			t.setDaemon(true);
			t.start();
		}
		
		// and a sender thread
		MessageSender sender = new MessageSender(Freemail.datadir);
		Thread senderthread = new Thread(sender, "Message sender");
		senderthread.setDaemon(true);
		senderthread.start();
		
		// start the SMTP Listener
		SMTPListener smtpl = new SMTPListener(sender);
		Thread smtpthread = new Thread(smtpl, "SMTP Listener");
		smtpthread.setDaemon(true);
		smtpthread.start();
		
		// start the delayed ACK inserter
		File ackdir = new File(globaldatadir, ACKDIR);
		AckProcrastinator.setAckDir(ackdir);
		AckProcrastinator ackinserter = new AckProcrastinator();
		Thread ackinsthread = new Thread(ackinserter, "Delayed ACK Inserter");
		ackinsthread.setDaemon(true);
		ackinsthread.start();
		
		
		// start the IMAP listener
		IMAPListener imapl = new IMAPListener();
		imapl.run();
	}
}
