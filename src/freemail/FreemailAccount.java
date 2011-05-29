/*
 * FreemailAccount.java
 * This file is part of Freemail, copyright (C) 2008 Dave Baker
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

package freemail;

import java.io.File;

import freemail.utils.PropsFile;

public class FreemailAccount {
	private final String identity;
	private final String nickname;
	private final File accdir;
	private final PropsFile accprops;
	private final MessageBank mb;
	
	FreemailAccount(String identity, String nickname, File _accdir, PropsFile _accprops) {
		this.identity = identity;
		this.nickname = nickname;
		accdir = _accdir;
		accprops = _accprops;
		mb = new MessageBank(this);
	}
	
	public String getUsername() {
		return identity;
	}
	
	public File getAccountDir() {
		return accdir;
	}
	
	public PropsFile getProps() {
		return accprops;
	}
	
	public MessageBank getMessageBank() {
		return mb;
	}

	public String getNickname() {
		return nickname;
	}
}
