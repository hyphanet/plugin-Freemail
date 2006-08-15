package freemail.fcp;

import java.io.IOException;
import java.net.Socket;

import freemail.config.ConfigClient;

public class FCPContext implements ConfigClient {
	private String hostname;
	private int port;
	
	public Socket getConn() throws IOException {
		return new Socket(this.hostname, this.port);
	}
	
	public void setConfigProp(String key, String val) {
		if (key.equalsIgnoreCase("fcp_host")) {
			hostname = val;
		} else if (key.equalsIgnoreCase("fcp_port")) {
			try {
				port = Integer.parseInt(val);
			} catch (NumberFormatException nfe) {
				// just leave it as it was
			}
		}
	}
}
