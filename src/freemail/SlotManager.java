/*
 * SlotManager.java
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

import java.util.Vector;
import java.util.Enumeration;

/** Manages sequences of slots which are polled for messages, keeping track of which
 *  ones still need to be checked, which ones are used and which have expired.
 */
public abstract class SlotManager {
	// how long we should keep checking a slot for which a successive slot has
	// had a message retrieved on
	private static final long SLOT_LIFETIME = 7 * 24 * 60 * 60 * 1000;
	private static final int DEFAULT_POLL_AHEAD = 3;

	// 'slots' contains all unused slots, in order for which there is a
	// higher slot that is used. If there are no such slots, it contains the
	// first free slot
	private Vector slots;
	private int nextSlotNum;
	private final SlotSaveCallback cb;
	private final Object userdata;
	private int pollAhead;
	
	protected SlotManager(SlotSaveCallback cb, Object userdata, String slotlist) {
		this.slots = new Vector();
		this.cb = cb;
		this.userdata = userdata;
		this.nextSlotNum = 0;
		this.pollAhead = DEFAULT_POLL_AHEAD;
		
		String parts[] = slotlist.split(",");
		int i;
		for (i = 0; i < parts.length; i++) {
			String[] parts2 = parts[i].split("=", 2);
			Slot s = new Slot();
			s.slot = parts2[0];
			if (parts2.length > 1)
				s.time_added = Long.parseLong(parts2[1]);
			else
				s.time_added = -1;
			
			this.slots.add(s);
		}
	}
	
	/** Set the number of slots to poll after the last free one
	 */
	public void setPollAhead(int pa) {
		this.pollAhead = pa;
	}
	
	/** Mark the last given slot as used
	 */
	public synchronized void slotUsed() {
		if (this.nextSlotNum <= this.slots.size()) {
			// it's one in the list. delete it and move the next
			// pointer down to point to the same one
			// (If nextSlotNum is 0, this should rightfully throw
			// an ArrayIndexOutOfBoundsException
			this.nextSlotNum--;
			Slot s = (Slot)this.slots.remove(this.nextSlotNum);
			// additionally, if it was the last one, we need to push
			// the next slot onto the end
			if (this.nextSlotNum == this.slots.size()) {
				s.slot = this.incSlot(s.slot);
				// time added is -1 since no subsequent slots
				// have been used
				s.time_added = -1;
				this.slots.add(s);
			}
		} else {
			// add all the slots before the used one that aren't already
			// in the list
			int i;
			Slot s = (Slot)this.slots.lastElement();
			int slots_start_size = this.slots.size();
			for (i = slots_start_size; i < this.nextSlotNum - 1; i++) {
				s.slot = this.incSlot(s.slot);
				s.time_added = System.currentTimeMillis();
				this.slots.add(s);
			}
			// increment to get the used slot...
			s.slot = this.incSlot(s.slot);
			// and again to get the one that nextSlotNum is pointing at
			s.slot = this.incSlot(s.slot);
			// ...and add that
			s.time_added = System.currentTimeMillis();
			this.slots.add(s);
		}
		this.saveSlots();
	}
	
	private void saveSlots() {
		StringBuffer buf = new StringBuffer();
		
		Enumeration e = this.slots.elements();
		boolean first = true;
		while (e.hasMoreElements()) {
			if (!first) buf.append(",");
			first = false;
			Slot s = (Slot)e.nextElement();
			buf.append(s.slot);
			if (s.time_added > 0)
				buf.append("=").append(Long.toString(s.time_added));
		}
		this.cb.saveSlots(buf.toString(), this.userdata);
	}
	
	/** Method provided by subclasses to return the next slot given any slot
	 */
	protected abstract String incSlot(String slot);
	
	public synchronized String getNextSlot() {
		String retval = null;
		
		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
			if (this.nextSlotNum >= this.slots.size() + this.pollAhead) {
				// you've reached the end
				retval = null;
			} else if (this.nextSlotNum >= this.slots.size()) {
				// we're into the unused slots. make one up.
				Slot s = (Slot)this.slots.lastElement();
				int i;
				retval = s.slot;
				for (i = this.slots.size(); i <= this.nextSlotNum; i++) {
					retval = this.incSlot(retval);
				}
			} else {
				// we're looking at an unused slot
				Slot s = (Slot) this.slots.get(this.nextSlotNum);
				// is this one too old?
				if (s.time_added > 0 && s.time_added < System.currentTimeMillis() - SLOT_LIFETIME && this.nextSlotNum != this.slots.size() - 1) {
					// this slot is too old. Forget it.
					this.slots.remove(this.nextSlotNum);
					tryAgain = true;
				} else {
					retval = s.slot;
				}
			}
		}
		
		this.nextSlotNum++;
		return retval;
	}
	
	private class Slot {
		String slot;
		long time_added;
	}
}
