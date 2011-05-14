/*
 * NaturalSlotManager.java
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

package freemail;

public class NaturalSlotManager extends SlotManager {
	NaturalSlotManager(SlotSaveCallback cb, Object userdata, String slotlist) {
		super(cb, userdata, slotlist);
	}

	protected String incSlot(String slot) {
		int s = Integer.parseInt(slot);
		
		s++;
		return Integer.toString(s);
	}
	
	public int getNextSlotNat() {
		String slot = super.getNextSlot();
		
		if (slot == null) return -1;
		
		return Integer.parseInt(slot);
	}
}
