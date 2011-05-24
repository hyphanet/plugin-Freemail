/*
 * InboundContact.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007 Alexander Lehmann
 * Copyright (C) 2009 Martin Nyhus
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

package freemail.transport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import org.archive.util.Base32;
import org.bouncycastle.crypto.digests.SHA256Digest;

import freemail.AckProcrastinator;
import freemail.MessageBank;
import freemail.Postman;
import freemail.SlotManager;
import freemail.SlotSaveCallback;
import freemail.fcp.ConnectionTerminatedException;
import freemail.fcp.FCPFetchException;
import freemail.fcp.HighLevelFCPClient;
import freemail.utils.Logger;
import freemail.utils.PropsFile;

public class Channel extends Postman {
	private static final int POLL_AHEAD = 6;
	private File channelDir;
	private PropsFile channelProps;

	public void fetch(MessageBank mb, HighLevelFCPClient fcpcli) {
		String slots = this.channelProps.get("fetchslot");
		if (slots == null) {
			Logger.error(this,"Contact "+this.channelDir.getName()+" is corrupt - account file has no 'fetchslot' entry!");
			// TODO: probably delete the contact. it's useless now.
			return;
		}

		HashSlotManager sm = new HashSlotManager(new ChannelSlotSaveImpl(channelProps, "fetchslot"), null, slots);
		sm.setPollAhead(POLL_AHEAD);

		String basekey = this.channelProps.get("commssk");
		if (basekey == null) {
			Logger.error(this,"Contact "+this.channelDir.getName()+" is corrupt - account file has no 'commssk' entry!");
			// TODO: probably delete the contact. it's useless now.
			return;
		}
		String slot;
		while ( (slot = sm.getNextSlot()) != null) {
			// the slot should be 52 characters long, since this is how long a 256 bit string ends up when base32 encoded.
			// (the slots being base32 encoded SHA-256 checksums)
			// TODO: remove this once the bug is ancient history, or if actually want to check the slots, do so in the SlotManagers.
			// a fix for the bug causing this (https://bugs.freenetproject.org/view.php?id=1087) was committed on Feb 4 2007,
			// anybody who has started using Freemail after that date is not affected.
			if(slot.length()!=52) {
				Logger.normal(this,"Ignoring malformed slot "+slot+" (probably due to previous bug). Please the fix the entry in "+this.channelDir);
				break;
			}
			String key = basekey+slot;

			Logger.minor(this,"Attempting to fetch mail on key "+key);
			File msg = null;
			try {
				msg = fcpcli.fetch(key);
			} catch (ConnectionTerminatedException cte) {
				return;
			} catch (FCPFetchException fe) {
				// XXX: Slot should be marked dead if this is a fatal error
				Logger.minor(this,"No mail in slot (fetch returned "+fe.getMessage()+")");
				continue;
			}
			Logger.normal(this,"Found a message!");

			// parse the Freemail header(s) out.
			PropsFile msgprops = PropsFile.createPropsFile(msg, true);
			String s_id = msgprops.get("id");
			if (s_id == null) {
				Logger.error(this,"Got a message with an invalid header. Discarding.");
				sm.slotUsed();
				msgprops.closeReader();
				msg.delete();
				continue;
			}

			int id;
			try {
				id = Integer.parseInt(s_id);
			} catch (NumberFormatException nfe) {
				Logger.error(this,"Got a message with an invalid (non-integer) id. Discarding.");
				sm.slotUsed();
				msgprops.closeReader();
				msg.delete();
				continue;
			}

			MessageLog msglog = new MessageLog(this.channelDir);
			boolean isDupe;
			try {
				isDupe = msglog.isPresent(id);
			} catch (IOException ioe) {
				Logger.error(this,"Couldn't read logfile, so don't know whether received message is a duplicate or not. Leaving in the queue to try later.");
				msgprops.closeReader();
				msg.delete();
				continue;
			}
			if (isDupe) {
				Logger.normal(this,"Got a message, but we've already logged that message ID as received. Discarding.");
				sm.slotUsed();
				msgprops.closeReader();
				msg.delete();
				continue;
			}

			BufferedReader br = msgprops.getReader();
			if (br == null) {
				Logger.error(this,"Got an invalid message. Discarding.");
				sm.slotUsed();
				msgprops.closeReader();
				msg.delete();
				continue;
			}

			try {
				this.storeMessage(br, mb);
				msg.delete();
			} catch (IOException ioe) {
				msg.delete();
				continue;
			} catch (ConnectionTerminatedException cte) {
				// terminated before we could validate the sender. Give up, and we won't mark the slot used so we'll
				// pick it up next time.
				return;
			}
			Logger.normal(this,"You've got mail!");
			sm.slotUsed();
			try {
				msglog.add(id);
			} catch (IOException ioe) {
				// how should we handle this? Remove the message from the inbox again?
				Logger.error(this,"warning: failed to write log file!");
			}
			String ack_key = this.channelProps.get("ackssk");
			if (ack_key == null) {
				Logger.error(this,"Warning! Can't send message acknowledgement - don't have an 'ackssk' entry! This message will eventually bounce, even though you've received it.");
				continue;
			}
			ack_key += "ack-"+id;
			AckProcrastinator.put(ack_key);
		}
	}

	private class HashSlotManager extends SlotManager {
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

	private static class MessageLog {
		private static final String LOGFILE = "log";
		private final File logfile;

		public MessageLog(File ibctdir) {
			this.logfile = new File(ibctdir, LOGFILE);
		}

		public boolean isPresent(int targetid) throws IOException {
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(this.logfile));
			} catch (FileNotFoundException fnfe) {
				return false;
			}

			String line;
			while ( (line = br.readLine()) != null) {
				int curid = Integer.parseInt(line);
				if (curid == targetid) {
					br.close();
					return true;
				}
			}

			br.close();
			return false;
		}

		public void add(int id) throws IOException {
			FileOutputStream fos = new FileOutputStream(this.logfile, true);

			PrintStream ps = new PrintStream(fos);
			ps.println(id);
			ps.close();
		}
	}

	private static class ChannelSlotSaveImpl implements SlotSaveCallback {
		private final PropsFile propsFile;
		private final String keyName;

		private ChannelSlotSaveImpl(PropsFile propsFile, String keyName) {
			this.propsFile = propsFile;
			this.keyName = keyName;
		}

		@Override
		public void saveSlots(String slots, Object userdata) {
			propsFile.put(keyName, slots);
		}
	}
}
