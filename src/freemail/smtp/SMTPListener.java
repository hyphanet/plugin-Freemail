/*
 * SMTPListener.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007 Alexander Lehmann
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

package freemail.smtp;

import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.IOException;

import freemail.AccountManager;
import freemail.MessageSender;
import freemail.ServerListener;
import freemail.config.ConfigClient;
import freemail.config.Configurator;
import freemail.utils.Logger;

public class SMTPListener extends ServerListener implements Runnable,ConfigClient {
	private static final int LISTENPORT = 3025;
	private final MessageSender msgsender;
	private String bindaddress;
	private int bindport;
	private final AccountManager accountManager;
	
	public SMTPListener(AccountManager accMgr, MessageSender sender, Configurator cfg) {
		this.msgsender = sender;
		this.accountManager = accMgr;
		cfg.register(Configurator.SMTP_BIND_ADDRESS, this, "127.0.0.1");
		cfg.register(Configurator.SMTP_BIND_PORT, this, Integer.toString(LISTENPORT));
	}
	
	@Override
	public void run() {
		try {
			this.realrun();
		} catch (IOException ioe) {
			Logger.error(this,"Error in SMTP server - "+ioe.getMessage());
		}
	}
	
	@Override
	public void setConfigProp(String key, String val) {
		if (key.equalsIgnoreCase(Configurator.SMTP_BIND_ADDRESS)) {
			this.bindaddress = val;
		} else if (key.equalsIgnoreCase(Configurator.SMTP_BIND_PORT)) {
			this.bindport = Integer.parseInt(val);
		}
	}
	
	public void realrun() throws IOException {
		sock = new ServerSocket(this.bindport, 10, InetAddress.getByName(this.bindaddress));
		while (!sock.isClosed()) {
			try {
				SMTPHandler newcli = new SMTPHandler(accountManager, sock.accept(), this.msgsender);
				Thread newthread = new Thread(newcli);
				newthread.setDaemon(true);
				newthread.start();
				addHandler(newcli, newthread);
			} catch (IOException ioe) {
				
			}
			
			reapHandlers();
		}
	}
}
