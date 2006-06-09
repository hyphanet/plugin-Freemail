package fnmail.imap;

import java.net.ServerSocket;
import java.io.IOException;

public class IMAPListener implements Runnable {
	private static final int LISTENPORT = 3143;

	public void run() {
		try {
			this.realrun();
		} catch (IOException ioe) {
			System.out.println("Error in IMAP server - "+ioe.getMessage());
		}
	}

	public void realrun() throws IOException {
		ServerSocket sock = new ServerSocket(LISTENPORT);
		
		while (!sock.isClosed()) {
			try {
				IMAPHandler newcli = new IMAPHandler(sock.accept());
				Thread newthread = new Thread(newcli);
				newthread.setDaemon(true);
 				newthread.start();
			} catch (IOException ioe) {
				
			}
		}
	}
}
