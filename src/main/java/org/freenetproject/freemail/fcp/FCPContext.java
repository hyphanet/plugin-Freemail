/*
 * FCPContext.java
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

package org.freenetproject.freemail.fcp;

import java.io.IOException;
import java.net.Socket;

import org.freenetproject.freemail.config.ConfigClient;
import org.freenetproject.freemail.config.Configurator;


public class FCPContext implements ConfigClient {
	private String hostname;
	private int port;

	public Socket getConn() throws IOException {
		return new Socket(this.hostname, this.port);
	}

	@Override
	public void setConfigProp(String key, String val) {
		if(key.equalsIgnoreCase(Configurator.FCP_HOST)) {
			hostname = val;
		} else if(key.equalsIgnoreCase(Configurator.FCP_PORT)) {
			try {
				port = Integer.parseInt(val);
			} catch (NumberFormatException nfe) {
				// just leave it as it was
			}
		}
	}
}
