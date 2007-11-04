package freemail;

import java.net.Socket;
import java.io.IOException;

public abstract class ServerHandler {
	protected final Socket client;
	
	public ServerHandler(Socket c) {
		client = c;
	}
	
	public boolean isAlive() {
		return !client.isClosed();
	}
	
	public void kill() {
		try {
			client.close();
		} catch (IOException ioe) {
			
		}
	}
}
