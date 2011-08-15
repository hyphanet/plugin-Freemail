/*
 * NumberedLock.java
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

package freemail.support;

import java.util.HashSet;
import java.util.Set;

public class NumberedLock {
	private final Set<Long> lockedNumbers = new HashSet<Long>();

	public void lock(long lock) {
		synchronized(lockedNumbers) {
			while(lockedNumbers.contains(Long.valueOf(lock))) {
				try {
					lockedNumbers.wait();
				} catch(InterruptedException e) {
					//Set the interrupt status so the caller can check it
					Thread.currentThread().interrupt();
				}
			}

			lockedNumbers.add(Long.valueOf(lock));
		}
	}

	public void unlock(long lock) {
		synchronized(lockedNumbers) {
			lockedNumbers.remove(Long.valueOf(lock));
			lockedNumbers.notifyAll();
		}
	}
}
