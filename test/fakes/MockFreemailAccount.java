/*
 * MockFreemailAccount.java
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

import java.io.File;

import org.freenetproject.freemail.Freemail;
import org.freenetproject.freemail.NullFreemailAccount;
import org.freenetproject.freemail.utils.PropsFile;

public class MockFreemailAccount extends NullFreemailAccount {
	private final String identity;
	private final File accountDir;
	private final PropsFile propsFile;

	public MockFreemailAccount(String identity, File accountDir, PropsFile propsFile, Freemail freemail) {
		super(identity, accountDir, propsFile, freemail);
		this.identity = identity;
		this.accountDir = accountDir;
		this.propsFile = propsFile;
	}

	@Override
	public File getAccountDir() {
		return accountDir;
	}

	@Override
	public String getIdentity() {
		return identity;
	}

	@Override
	public PropsFile getProps() {
		return propsFile;
	}
}
