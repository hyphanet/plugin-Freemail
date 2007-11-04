package freemail;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class ServerListener {
	protected ServerSocket sock;
	private final ArrayList /* of ServerHandler */ handlers;
	private final ArrayList /* of Thread */ handlerThreads;
	
	protected ServerListener() {
		handlers = new ArrayList();
		handlerThreads = new ArrayList();
	}
	
	/**
	 * Terminate the run method
	 */
	public void kill() {
		try {
			sock.close();
		} catch (IOException ioe) {
			
		}
		// kill all our handlers too
		for (Iterator i = handlers.iterator(); i.hasNext(); ) {
			ServerHandler handler =(ServerHandler) i.next();
			handler.kill();
		}
	}
	
	/**
	 * Wait for all our client threads to terminate
	 */
	public void joinClientThreads() {
		for (Iterator i = handlerThreads.iterator(); i.hasNext(); ) {
			Thread t = (Thread)i.next();
			while (t != null) {
				try {
					t.join();
					t = null;
				} catch (InterruptedException ie) {
					
				}
			}
		}
	}
	
	protected void addHandler(ServerHandler hdlr, Thread thrd) {
		handlers.add(hdlr);
		handlerThreads.add(thrd);
	}
	
	protected void reapHandlers() {
		// clean up dead handlers...
		for (Iterator i = handlers.iterator(); i.hasNext(); ) {
			ServerHandler handler = (ServerHandler)i.next(); 
			if (!handler.isAlive()) {
				i.remove();
			}
		}
		
		// ...and threads...
		for (Iterator i = handlerThreads.iterator(); i.hasNext(); ) {
			Thread t = (Thread)i.next();
			if (!t.isAlive()) {
				i.remove();
			}
		}
	}
}
