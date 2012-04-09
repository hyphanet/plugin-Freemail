/*
 * Timer.java
 * This file is part of Freemail
 * Copyright (C) 2012 Martin Nyhus
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

package freemail.utils;

public final class Timer {
	private final long startTime;

	private Timer() {
		startTime = System.nanoTime();
	}

	public static Timer start() {
		return new Timer();
	}

	public long getTime() {
		long cur = System.nanoTime();
		return Math.abs(cur - startTime);
	}

	public void log(Class<?> c, String message) {
		long time = getTime();
		Logger.minor(c, message + ": " + time + "ns");
	}

	public void log(Object o, String message) {
		long time = getTime();
		Logger.minor(o, message + ": " + time + "ns");
	}
}
