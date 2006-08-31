package freemail;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import freemail.FreenetURI;
import freemail.utils.PropsFile;
import freemail.utils.EmailAddress;
import freemail.fcp.HighLevelFCPClient;

import org.archive.util.Base32;

public class InboundContact extends Postman implements SlotSaveCallback {
	private static final String IBCT_PROPSFILE = "props";
	// how many slots should we poll past the last occupied one?
	private static final int POLL_AHEAD = 6;
	private File ibct_dir;
	private PropsFile ibct_props;
	
	public InboundContact(File contact_dir, FreenetURI mailsite) {
		this(contact_dir, mailsite.getKeyBody());
	}

	public InboundContact(File contact_dir, String keybody) {
		this.ibct_dir = new File(contact_dir, keybody);
		
		if (!this.ibct_dir.exists()) {
			this.ibct_dir.mkdir();
		}
		
		this.ibct_props = new PropsFile(new File(this.ibct_dir, IBCT_PROPSFILE));
	}
	
	public void setProp(String key, String val) {
		this.ibct_props.put(key, val);
	}
	
	public String getProp(String key) {
		return this.ibct_props.get(key);
	}
	
	public void fetch(MessageBank mb) {
		HighLevelFCPClient fcpcli = new HighLevelFCPClient();
		
		String slots = this.ibct_props.get("slots");
		if (slots == null) {
			System.out.println("Contact "+this.ibct_dir.getName()+" is corrupt - account file has no 'slots' entry!");
			// TODO: probably delete the contact. it's useless now.
			return;
		}
		
		HashSlotManager sm = new HashSlotManager(this, null, slots);
		sm.setPollAhead(POLL_AHEAD);
		
		String basekey = this.ibct_props.get("commssk");
		if (basekey == null) {
			System.out.println("Contact "+this.ibct_dir.getName()+" is corrupt - account file has no 'commssk' entry!");
			// TODO: probably delete the contact. it's useless now.
			return;
		}
		String slot;
		while ( (slot = sm.getNextSlot()) != null) {
			String key = basekey+slot;
			
			System.out.println("Attempting to fetch mail on key "+key);
			File msg = fcpcli.fetch(key);
			if (msg == null) {
				System.out.println("No mail there.");
				continue;
			}
			System.out.println("Found a message!");
			
			// parse the Freemail header(s) out.
			PropsFile msgprops = new PropsFile(msg, true);
			String s_id = msgprops.get("id");
			if (s_id == null) {
				System.out.println("Got a message with an invalid header. Discarding.");
				sm.slotUsed();
				msgprops.closeReader();
				msg.delete();
				continue;
			}
			
			int id;
			try {
				id = Integer.parseInt(s_id);
			} catch (NumberFormatException nfe) {
				System.out.println("Got a message with an invalid (non-integer) id. Discarding.");
				sm.slotUsed();
				msgprops.closeReader();
				msg.delete();
				continue;
			}
			
			MessageLog msglog = new MessageLog(this.ibct_dir);
			boolean isDupe;
			try {
				isDupe = msglog.isPresent(id);
			} catch (IOException ioe) {
				System.out.println("Couldn't read logfile, so don't know whether received message is a duplicate or not. Leaving in the queue to try later.");
				msgprops.closeReader();
				msg.delete();
				continue;
			}
			if (isDupe) {
				System.out.println("Got a message, but we've already logged that message ID as received. Discarding.");
				sm.slotUsed();
				msgprops.closeReader();
				msg.delete();
				continue;
			}
			
			BufferedReader br = msgprops.getReader();
			if (br == null) {
				System.out.println("Got an invalid message. Discarding.");
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
			}
			System.out.println("You've got mail!");
			sm.slotUsed();
			try {
			    msglog.add(id);
			} catch (IOException ioe) {
			    // how should we handle this? Remove the message from the inbox again?
			    System.out.println("warning: failed to write log file!");
			}
			String ack_key = this.ibct_props.get("ackssk");
			if (ack_key == null) {
				System.out.println("Warning! Can't send message acknowledgement - don't have an 'ackssk' entry! This message will eventually bounce, even though you've received it.");
				continue;
			}
			ack_key += "ack-"+id;
			AckProcrastinator.put(ack_key);
		}
	}
	
	public void saveSlots(String s, Object userdata) {
		this.ibct_props.put("slots", s);
	}
	
	public boolean validateFrom(EmailAddress from) throws IOException {
		String sd = from.getSubDomain();
		
		if (from.is_ssk_address()) {
			return Base32.encode(this.ibct_dir.getName().getBytes()).equalsIgnoreCase(sd);
		} else {
			// try to fetch that KSK redirect address
			HighLevelFCPClient cli = new HighLevelFCPClient();
			
			// quick sanity check
			if (sd.indexOf("\r") > 0 || sd.indexOf("\n") > 0) return false;
			
			File result = cli.fetch("KSK@"+sd+MailSite.ALIAS_SUFFIX);
			
			if (result == null) {
				// we just received the message so we can assume our
				// network connection is healthy, and the mailsite
				// ought to be easily retrievable, so fail.
				// If this proves to be an issue, change it.
				return false;
			}
			if (result.length() > 512) {
				result.delete();
				return false;
			}
			BufferedReader br = new BufferedReader(new FileReader(result));
			
			String line = br.readLine();
			br.close();
			result.delete();
			FreenetURI furi;
			try {
				furi = new FreenetURI(line);
			} catch (MalformedURLException mfue) {
				return false;
			}
			return this.ibct_dir.getName().equals(furi.getKeyBody());
		}
	}
	
	
	private class MessageLog {
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
				if (curid == targetid) return true;
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
}
