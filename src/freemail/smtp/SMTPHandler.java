package freemail.smtp;

import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import thirdparty.Base64Coder;
import freemail.Freemail;
import freemail.AccountManager;
import freemail.MessageSender;
import freemail.utils.EmailAddress;

public class SMTPHandler implements Runnable {
	private final Socket client;
	private final OutputStream os;
	private final PrintStream ps;
	private final BufferedReader bufrdr;
	private String username;
	private final MessageSender msgsender;
	public static final String MY_HOSTNAME = "localhost";
	
	private Vector to;
	
	public SMTPHandler(Socket client, MessageSender sender) throws IOException {
		this.client = client;
		this.msgsender = sender;
		this.username = null;
		this.os = client.getOutputStream();
		this.ps = new PrintStream(this.os);
		this.bufrdr = new BufferedReader(new InputStreamReader(client.getInputStream()));
		
		this.to = new Vector();
	}
	
	public void run() {
		this.sendWelcome();
		
		String line;
		try {
			while ( !this.client.isClosed() && (line = this.bufrdr.readLine()) != null) {
				SMTPCommand msg = null;
				try {
					//System.out.println(line);
					msg = new SMTPCommand(line);
				} catch (SMTPBadCommandException bce) {
					continue;
				}
				
				this.dispatch(msg);
			}
		
			this.client.close();
		} catch (IOException ioe) {
			
		}
	}
	
	private void dispatch(SMTPCommand cmd) {
		//System.out.println(cmd.toString());
		if (cmd.command.equals("helo")) {
			this.handle_helo(cmd);
		} else if (cmd.command.equals("ehlo")) {
			this.handle_ehlo(cmd);
		} else if (cmd.command.equals("quit")) {
			this.handle_quit(cmd);
		} else if (cmd.command.equals("turn")) {
			this.handle_turn(cmd);
		} else if (cmd.command.equals("auth")) {
			this.handle_auth(cmd);
		} else if (cmd.command.equals("mail")) {
			this.handle_mail(cmd);
		} else if (cmd.command.equals("rcpt")) {
			this.handle_rcpt(cmd);
		} else if (cmd.command.equals("data")) {
			this.handle_data(cmd);
		} else if (cmd.command.equals("rset")) {
			this.handle_rset(cmd);
		} else {
			this.ps.print("502 Unimplemented\r\n");
		}
	}
	
	private void handle_helo(SMTPCommand cmd) {
		this.ps.print("250 "+MY_HOSTNAME+"\r\n");
	}
	
	private void handle_ehlo(SMTPCommand cmd) {
		this.ps.print("250-"+MY_HOSTNAME+"\r\n");
		this.ps.print("250 AUTH LOGIN PLAIN \r\n");
	}
	
	private void handle_quit(SMTPCommand cmd) {
		this.ps.print("221 "+MY_HOSTNAME+"\r\n");
		try {
			this.client.close();
		} catch (IOException ioe) {
			
		}
	}
	
	private void handle_turn(SMTPCommand cmd) {
		this.ps.print("502 No\r\n");
	}
	
	private void handle_auth(SMTPCommand cmd) {
		String uname;
		String password;
		
		if (cmd.args.length == 0) {
			this.ps.print("504 No auth type given\r\n");
			return;
		} else if (cmd.args[0].equalsIgnoreCase("login")) {
			this.ps.print("334"+Base64Coder.encode("Username:")+"\r\n");
			
			String b64username;
			String b64password;
			try {
				b64username = this.bufrdr.readLine();
			} catch (IOException ioe) {
				return;
			}
			if (b64username == null) return;
			
			this.ps.print("334"+Base64Coder.encode("Password:")+"\r\n");
			try {
				b64password = this.bufrdr.readLine();
			} catch (IOException ioe) {
				return;
			}
			if (b64password == null) return;
			
			uname = Base64Coder.decode(b64username);
			password = Base64Coder.decode(b64password);
		} else if (cmd.args[0].equalsIgnoreCase("plain")) {
			String b64creds;
			
			if (cmd.args.length > 1) {
				b64creds = cmd.args[1];
			} else {
				this.ps.print("334 Credentials:\r\n");
				try {
					b64creds = this.bufrdr.readLine();
					if (b64creds == null) return;
				} catch (IOException ioe) {
					return;
				}
			}
			
			String[] creds = Base64Coder.decode(b64creds).split("\0");
			
			if (creds.length < 2) return;
			
			// most documents seem to reckon you send the
			// username twice. Some think only once.
			// This will work either way.
			uname = creds[0];
			password = creds[creds.length - 1];
		} else {
			this.ps.print("504 Auth type unimplemented - weren't you listening?\r\n");
			return;
		}
		
		if (AccountManager.authenticate(uname, password)) {
			this.username = uname;
			
			this.ps.print("235 Authenticated\r\n");
		} else {
			this.ps.print("535 Authentication failed\r\n");
		}
	}
	
	private void handle_mail(SMTPCommand cmd) {
		if (this.username == null) {
			this.ps.print("530 Authentication required\r\n");
			return;
		}
		
		// we don't really care.
		this.ps.print("250 OK\r\n");
	}
	
	private void handle_rcpt(SMTPCommand cmd) {
		if (cmd.args.length < 1) {
			this.ps.print("504 Insufficient arguments\r\n");
			return;
		}
		
		if (this.username == null) {
			this.ps.print("530 Authentication required\r\n");
			return;
		}
		
		String[] parts = cmd.args[0].split(":", 2);
		if (parts.length < 2) {
			this.ps.print("504 Can't understand that syntax\r\n");
			return;
		}
		
		EmailAddress addr = new EmailAddress(parts[1]);
		if (addr.user == null || addr.domain == null) {
			this.ps.print("504 Bad address\r\n");
			return;
		}
		
		if (!addr.is_freemail_address()) {
			this.ps.print("553 Not a Freemail address\r\n");
			return;
		}
		
		
		this.to.add(addr);
		
		this.ps.print("250 OK\r\n");
	}
	
	private void handle_data(SMTPCommand cmd) {
		if (this.username == null) {
			this.ps.print("530 Authentication required\r\n");
			return;
		}
		
		if (this.to.size() == 0) {
			this.ps.print("503 RCPT first\r\n");
			return;
		}
		
		try {
			File tempfile = File.createTempFile("freemail-", ".message", Freemail.getTempDir());
			PrintWriter pw = new PrintWriter(new FileOutputStream(tempfile));
			
			this.ps.print("354 Go crazy\r\n");
			
			String line;
			boolean done = false;
			while ( (line = this.bufrdr.readLine()) != null) {
				if (line.equals(".")) {
					done = true;
					break;
				}
				pw.print(line+"\r\n");
			}
			
			pw.close();
			if (!done) {
				// connection closed before the message was
				// finished. bail out.
				tempfile.delete();
				return;
			}
			
			this.msgsender.send_message(this.username, to, tempfile);
			
			tempfile.delete();
			
			this.ps.print("250 So be it\r\n");
		} catch (IOException ioe) {
			this.ps.print("452 Can't store message\r\n");
		}
	}
	
	private void handle_rset(SMTPCommand cmd) {
		this.to.clear();
		this.ps.print("250 Reset\r\n");
	}
	
	private void sendWelcome() {
		this.ps.print("220 "+MY_HOSTNAME+" ready\r\n");
	}
}
