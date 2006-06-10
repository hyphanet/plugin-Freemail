package freemail.smtp;

import java.net.ServerSocket;
import java.io.IOException;

import freemail.MessageSender;

public class SMTPListener implements Runnable {
	private static final int LISTENPORT = 3025;
	private final MessageSender msgsender;
	
	public SMTPListener(MessageSender sender) {
		this.msgsender = sender;
	}
	
	public void run() {
		try {
			this.realrun();
		} catch (IOException ioe) {
			System.out.println("Error in SMTP server - "+ioe.getMessage());
		}
	}
	
	public void realrun() throws IOException {
		ServerSocket sock = new ServerSocket(LISTENPORT);
		
		while (!sock.isClosed()) {
			try {
				SMTPHandler newcli = new SMTPHandler(sock.accept(), this.msgsender);
				Thread newthread = new Thread(newcli);
				newthread.setDaemon(true);
				newthread.start();
			} catch (IOException ioe) {
				
			}
		}
	}
}
