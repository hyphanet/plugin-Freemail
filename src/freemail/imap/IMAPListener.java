package freemail.imap;

import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.IOException;

import freemail.config.Configurator;
import freemail.config.ConfigClient;

public class IMAPListener implements Runnable,ConfigClient {
	private static final int LISTENPORT = 3143;
	private String bindaddress;
	private int bindport;
	
	public IMAPListener(Configurator cfg) {
		cfg.register("imap_bind_address", this, "127.0.0.1");
		cfg.register("imap_bind_port", this, Integer.toString(LISTENPORT));
	}
	
	public void setConfigProp(String key, String val) {
		if (key.equalsIgnoreCase("imap_bind_address")) {
			this.bindaddress = val;
		} else if (key.equalsIgnoreCase("imap_bind_port")) {
			this.bindport = Integer.parseInt(val);
		}
	}
	
	public void run() {
		try {
			this.realrun();
		} catch (IOException ioe) {
			System.out.println("Error in IMAP server - "+ioe.getMessage());
		}
	}

	public void realrun() throws IOException {
		ServerSocket sock = new ServerSocket(this.bindport, 10, InetAddress.getByName(this.bindaddress));
		
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
