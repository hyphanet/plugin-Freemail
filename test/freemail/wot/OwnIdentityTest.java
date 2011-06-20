/*
 * OwnIdentityTest.java
 * This file is part of Freemail
 * Copyright (C) 2011 Martin Nyhus
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

package freemail.wot;

import junit.framework.TestCase;

public class OwnIdentityTest extends TestCase {
	public void testEqualsSymmetricWithIdentity() {
		final String identityID = "test";
		final String nickname = "test";
		final String requestURI = "test";
		final String insertURI = "test";

		Identity identity = new Identity(identityID, requestURI, nickname);
		OwnIdentity ownIdentity = new OwnIdentity(identityID, requestURI, insertURI, nickname);

		assertTrue(identity.equals(ownIdentity));
		assertTrue(ownIdentity.equals(identity));
	}
}
