/*
 * Freemail.java
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

package freemail;

import java.io.File;
import freemail.fcp.FCPConnection;
import freemail.config.ConfigClient;

public class Freemail implements ConfigClient {
	public static final int VER_MAJOR = 0;
	public static final int VER_MINOR = 1;
	public static final int BUILD_NO = 8;
	public static final String VERSION_TAG = "Pet Shop";

	protected static final String TEMPDIRNAME = "temp";
	protected static final String DATADIR = "data";
	protected static final String GLOBALDATADIR = "globaldata";
	protected static final String ACKDIR = "delayedacks";
	protected static final String CFGFILE = "globalconfig";
	private static File datadir;
	private static File globaldatadir;
	private static File tempdir;
	protected static FCPConnection fcpconn = null;
	
	public static File getTempDir() {
		return Freemail.tempdir;
	}
	
	protected static File getGlobalDataDir() {
		return globaldatadir;
	}
	
	public static File getDataDir() {
		return datadir;
	}
	
	public static FCPConnection getFCPConnection() {
		return Freemail.fcpconn;
	}

	public void setConfigProp(String key, String val) {
		if (key.equalsIgnoreCase("datadir")) {
			Freemail.datadir = new File(val);
		} else if (key.equalsIgnoreCase("tempdir")) {
			Freemail.tempdir = new File(val);
		} else if (key.equalsIgnoreCase("globaldatadir")) {
			Freemail.globaldatadir = new File(val);
		}
	}
}
