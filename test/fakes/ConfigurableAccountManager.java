/*
 * ConfigurableAccountManager.java
 * This file is part of Freemail
 * Copyright (C) 2011,2012 Martin Nyhus
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

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Map;

import org.freenetproject.freemail.Freemail;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.utils.PropsFile;


public class ConfigurableAccountManager extends NullAccountManager {
	private final Map<String, File> accountDirs;
	private final boolean failAuth;

	public ConfigurableAccountManager(File datadir, boolean failAuth, Map<String, File> accountDirs) {
		super(datadir, null);

		this.accountDirs = accountDirs;
		this.failAuth = failAuth;
	}

	@Override
	public FreemailAccount authenticate(String username, String password) {
		if(failAuth) return null;

		File accountDir = accountDirs.get(username);
		if(accountDir == null) {
			return null;
		}

		//FreemailAccount constructor is package-protected and
		//there is no reason to change that, so use reflection
		//to construct a new account
		try {
			Class<FreemailAccount> freemailAccount = FreemailAccount.class;
			Constructor<FreemailAccount> constructor =
					freemailAccount.getDeclaredConstructor(String.class, File.class, PropsFile.class, Freemail.class);
			constructor.setAccessible(true);
			return constructor.newInstance(username, accountDir, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());

			throw new AssertionError(); //Since the compiler doesn't know fail() never returns
		}
	}
}
