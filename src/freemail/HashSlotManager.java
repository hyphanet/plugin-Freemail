package freemail;

import org.archive.util.Base32;

import org.bouncycastle.crypto.digests.SHA256Digest;

public class HashSlotManager extends SlotManager {
	HashSlotManager(SlotSaveCallback cb, Object userdata, String slotlist) {
		super(cb, userdata, slotlist);
	}
	
	protected String incSlot(String slot) {
		byte[] buf = Base32.decode(slot);
		SHA256Digest sha256 = new SHA256Digest();
		sha256.update(buf, 0, buf.length);
		sha256.doFinal(buf, 0);
		
		return Base32.encode(buf);
	}
}
