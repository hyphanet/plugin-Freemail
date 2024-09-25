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

package org.freenetproject.freemail.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Timer {
	private final long startTime;
	private final boolean isSubTimer;

	private final List<Timer> subTimers = new LinkedList<Timer>();

	private String logMessage;
	private boolean logAtWarning = false;

	private Timer(boolean isSubTimer) {
		startTime = System.nanoTime();
		this.isSubTimer = isSubTimer;
	}

	public static Timer start() {
		return new Timer(false);
	}

	public long getTime() {
		long cur = System.nanoTime();
		return Math.abs(cur - startTime);
	}

	public void log(Class<?> c, String message) {
		long time = getTime();
		logMessage = message + ": " + time + "ns";

		if(!isSubTimer) {
			log(c);
		}
	}

	public void log(Object o, String message) {
		long time = getTime();
		logMessage = message + ": " + time + "ns";

		if(!isSubTimer) {
			log(o);
		}
	}

	public void log(Class<?> c, long warningThreshold, TimeUnit unit, String message) {
		long time = getTime();
		logMessage = message + ": " + time + "ns";

		if(time >= unit.toNanos(warningThreshold)) {
			logAtWarning = true;
		}

		if(!isSubTimer) {
			log(c);
		}
	}

	public void log(Object o, long warningThreshold, TimeUnit unit, String message) {
		long time = getTime();
		logMessage = message + ": " + time + "ns";

		if(time >= unit.toNanos(warningThreshold)) {
			logAtWarning = true;
		}

		if(!isSubTimer) {
			log(o);
		}
	}

	public Timer startSubTimer() {
		Timer t = new Timer(true);
		subTimers.add(t);
		return t;
	}

	private void log(Class<?> c) {
		for(Timer t : subTimers) {
			if(logAtWarning) {
				t.logAtWarning = true;
			}
			t.log(c);
		}

		if(logAtWarning) {
			Logger.warning(c, logMessage);
		} else {
			Logger.minor(c, logMessage);
		}
	}

	private void log(Object o) {
		for(Timer t : subTimers) {
			if(logAtWarning) {
				t.logAtWarning = true;
			}
			t.log(o);
		}

		if(logAtWarning) {
			Logger.warning(o, logMessage);
		} else {
			Logger.minor(o, logMessage);
		}
	}
}
