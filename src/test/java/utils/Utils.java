/*
 * Utils.java
 * This file is part of Freemail, copyright (C) 2011 Martin Nyhus
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

package utils;

import static org.junit.Assert.*;

import java.io.File;

public class Utils {
	public static boolean createDir(File dir) {
		assertFalse(dir.getAbsolutePath() + " already exists", dir.exists());
		boolean result = dir.mkdir();
		assertTrue("Couldn't create " + dir.getAbsolutePath(), result);
		return result;
	}

	public static File createDir(File parent, String name) {
		File dir = new File(parent, name);
		createDir(dir);
		return dir;
	}

	/**
	 * Deletes a File, including all its contents if it is a directory.
	 * Prints the path of any Files that can't be deleted to System.out
	 */
	public static boolean delete(File file) {
		if(!file.exists()) {
			return true;
		}

		if(!file.isDirectory()) {
			if(!file.delete()) {
				System.out.println("Failed to delete " + file);
				return false;
			}
			return true;
		}

		for(File f : file.listFiles()) {
			if(!delete(f)) {
				return false;
			}
		}

		return file.delete();
	}
}
