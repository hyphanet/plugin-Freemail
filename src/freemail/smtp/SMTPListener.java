/*
 * SMTPListener.java
 * This file is part of Freemail, copyright (C) 2006 Dave Baker
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * 
 */

package freemail.smtp;

import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.IOException;

import freemail.MessageSender;
import freemail.config.ConfigClient;
import freemail.config.Configurator;

public class SMTPListener implements Runnable,ConfigClient {
	private static final int LISTENPORT = 3025;
	private final MessageSender msgsender;
	private String bindaddress;
	private int bindport;
	
	public SMTPListener(MessageSender sender, Configurator cfg) {
		this.msgsender = sender;
		cfg.register("smtp_bind_address", this, "127.0.0.1");
		cfg.register("smtp_bind_port", this, Integer.toString(LISTENPORT));
	}
	
	public void run() {
		try {
			this.realrun();
		} catch (IOException ioe) {
			System.out.println("Error in SMTP server - "+ioe.getMessage());
		}
	}
	
	public void setConfigProp(String key, String val) {
		if (key.equalsIgnoreCase("smtp_bind_address")) {
			this.bindaddress = val;
		} else if (key.equalsIgnoreCase("smtp_bind_port")) {
			this.bindport = Integer.parseInt(val);
		}
	}
	
	public void realrun() throws IOException {
		ServerSocket sock = new ServerSocket(this.bindport, 10, InetAddress.getByName(this.bindaddress));
		
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
