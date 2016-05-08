/*
 * Version.java
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

package org.freenetproject.freemail;

public class Version {
	/** The human readable version */
	public static final String VERSION = "0.2.7.2";

	/**
	 * The build number, used by the plugin auto-updater. This must always
	 * increase, and at least by one per build that is uploaded to the auto
	 * update system.
	 */
	public static final int BUILD_NO = 27;

	/** Version number updated at build time using git describe */
	public static final String GIT_REVISION = "@custom@";

	public static String getVersionString() {
		if(GIT_REVISION.equals("v" + VERSION)) {
			//Presumably because this is a proper release,
			//so don't include the redundant git info
			return VERSION;
		} else {
			return VERSION + " (" + GIT_REVISION + ")";
		}
	}
}
