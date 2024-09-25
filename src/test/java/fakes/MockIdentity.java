/*
 * MockIdentity.java
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

import org.archive.util.Base32;
import org.freenetproject.freemail.utils.Logger;

import freenet.support.Base64;
import freenet.support.IllegalBase64Exception;

public class MockIdentity extends NullIdentity {
	private final String identityID;
	private final String requestURI;

	public MockIdentity(String identityID, String requestURI, String nickname) {
		super(identityID, requestURI, nickname);
		this.identityID = identityID;
		this.requestURI = requestURI;
	}

	@Override
	public String getRequestURI() {
		Logger.debug(this, "getRequestURI()");
		return requestURI;
	}

	@Override
	public String getBase32IdentityID() {
		Logger.debug(this, "getBase32IdentityID()");
		try {
			return Base32.encode(Base64.decode(identityID));
		} catch (IllegalBase64Exception e) {
			throw new AssertionError();
		}
	}

	@Override
	public String getIdentityID() {
		Logger.debug(this, "getIdentityID()");
		return identityID;
	}
}
