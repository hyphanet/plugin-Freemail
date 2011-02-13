/*
 * Version.java
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

public class Version {
	public static final int VER_MAJOR = 0;
	public static final int VER_MINOR = 1;
	public static final int BUILD_NO = 13;
	public static final String VERSION_TAG = "Pet Shop";

	// This will be replaced at build time by the correct value
	public static final String GIT_REVISION = "@custom@";

	public static String getVersionString() {
		return VER_MAJOR + "." + VER_MINOR + " " + VERSION_TAG + " (" + GIT_REVISION + ")";
	}
}
