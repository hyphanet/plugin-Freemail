/*
 * MockFreemail.java
 * This file is part of Freemail
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

package fakes;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.Freemail;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.wot.WoTConnection;

public class MockFreemail extends Freemail {
	private final WoTConnection wotConnection;

	public MockFreemail(String cfgfile, WoTConnection wotConnection) throws IOException {
		super(cfgfile);
		this.wotConnection = wotConnection;
	}

	@Override
	public WoTConnection getWotConnection() {
		Logger.debug(this, "getWotConnection()");
		return wotConnection;
	}

	@Override
	public AccountManager getAccountManager() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void startFcp() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void startServers(boolean daemon) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void startWorkers() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void updateFileFormat() {
		throw new UnsupportedOperationException();
	}

	//Since we want full control we override all the public methods. For the ones below we
	//want the default behaviour, but leave them in to clarify what we want

	@Override
	public void setConfigProp(String key, String val) {
		Logger.debug(this, "setConfigProp(key=" + key + ", val=" + val + ")");
		super.setConfigProp(key, val);
	}

	@Override
	public void terminate() {
		Logger.debug(this, "terminate()");
		super.terminate();
	}

	@Override
	public ScheduledExecutorService getExecutor(TaskType type) {
		Logger.debug(this, "getExecutor(type=" + type + ")");
		return super.getExecutor(type);
	}
}
