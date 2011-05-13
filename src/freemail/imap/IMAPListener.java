/*
 * IMAPListener.java
 * This file is part of Freemail, copyright (C) 2006 Dave Baker
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package freemail.imap;

import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.io.IOException;

import freemail.AccountManager;
import freemail.ServerListener;
import freemail.config.Configurator;
import freemail.config.ConfigClient;
import freemail.utils.Logger;

public class IMAPListener extends ServerListener implements Runnable,ConfigClient {
	private static final int LISTENPORT = 3143;
	private String bindaddress;
	private int bindport;
	private final AccountManager accountManager;
	
	public IMAPListener(AccountManager accMgr, Configurator cfg) {
		accountManager = accMgr;
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
			Logger.error(this,"Error in IMAP server - "+ioe.getMessage());
		}
	}

	public void realrun() throws IOException {
		sock = new ServerSocket(this.bindport, 10, InetAddress.getByName(this.bindaddress));
		sock.setSoTimeout(60000);
		while (!sock.isClosed()) {
			try {
				IMAPHandler newcli = new IMAPHandler(accountManager, sock.accept());
				Thread newthread = new Thread(newcli);
				newthread.setDaemon(true);
 				newthread.start();
 				addHandler(newcli, newthread);
			} catch (SocketTimeoutException ste) {
				
			} catch (IOException ioe) {
				
			}
			
			reapHandlers();
		}
	}
}
