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
