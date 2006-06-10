package freemail.fcp;

import java.io.IOException;
import java.net.Socket;

public class FCPContext {
	private final String hostname;
	private final int port;
	
	public FCPContext(String h, int p) {
		this.hostname = h;
		this.port = p;
	}
	
	public Socket getConn() throws IOException {
		return new Socket(this.hostname, this.port);
	}
}
