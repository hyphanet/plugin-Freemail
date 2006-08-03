package freemail.fcp;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

import freemail.Freemail;

public class HighLevelFCPClient implements FCPClient {
	private static final int FCP_PERMANANT_REDIRECT = 27;

	private FCPConnection conn;
	private FCPMessage donemsg;
	
	public HighLevelFCPClient() {
		this.conn = Freemail.getFCPConnection();
	}
	
	// It's up to the client to delete this File once they're
	// done with it
	public synchronized File fetch(String key) {
		FCPMessage msg = this.conn.getMessage("ClientGet");
		msg.headers.put("URI", key);
		msg.headers.put("ReturnType", "direct");
		msg.headers.put("Persistence", "connection");
		
		while (true) {
			try {
				this.conn.doRequest(this, msg);
				break;
			} catch (NoNodeConnectionException nnce) {
				try {
					System.out.println("got no conn exception: "+nnce.getMessage());
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
				}
			} catch (FCPBadFileException bfe) {
				// won't be thrown since this is a get,
				// but keep the compiler happy
			}
		}
		
		this.donemsg = null;
		while (this.donemsg == null) {
			try {
				this.wait();
			} catch (InterruptedException ie) {
			}
		}
		
		if (this.donemsg.getType().equalsIgnoreCase("AllData")) {
			return this.donemsg.getData();
		} else if (this.donemsg.getType().equalsIgnoreCase("GetFailed")) {
			String s_code = (String)this.donemsg.headers.get("Code");
			if (s_code == null) return null;
			int code = Integer.parseInt(s_code);
			if (code == FCP_PERMANANT_REDIRECT) {
				String newuri = (String) this.donemsg.headers.get("RedirectURI");
				if (newuri == null) return null;
				return this.fetch(newuri);
			}
			return null;
		} else {
			return null;
		}
	}
	
	public synchronized SSKKeyPair makeSSK() {
		FCPMessage msg = this.conn.getMessage("GenerateSSK");
		
		while (true) {
			try {
				this.conn.doRequest(this, msg);
				break;
			} catch (NoNodeConnectionException nnce) {
				try {
					System.out.println("Warning - no connection to node. Waiting...");
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
				}
			} catch (FCPBadFileException bfe) {
				// won't be thrown since no data
			}
		}
		
		this.donemsg = null;
		while (this.donemsg == null) {
			try {
				this.wait();
			} catch (InterruptedException ie) {
			}
		}
		
		if (this.donemsg.getType().equalsIgnoreCase("SSKKeypair")) {
			SSKKeyPair retval = new SSKKeyPair();
			
			retval.privkey = (String)this.donemsg.headers.get("InsertURI");
			retval.pubkey = (String)this.donemsg.headers.get("RequestURI");
			return retval;
		} else {
			return null;
		}
	}
	
	public synchronized FCPInsertErrorMessage put(InputStream data, String key) throws FCPBadFileException {
		FCPMessage msg = this.conn.getMessage("ClientPut");
		msg.headers.put("URI", key);
		msg.headers.put("Persistence", "connection");
		msg.setData(data);
		
		while (true) {
			try {
				this.conn.doRequest(this, msg);
				break;
			} catch (NoNodeConnectionException nnce) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
				}
			}
		}
		
		this.donemsg = null;
		while (this.donemsg == null) {
			try {
				this.wait();
			} catch (InterruptedException ie) {
			}
		}
		
		if (this.donemsg.getType().equalsIgnoreCase("PutSuccessful")) {
			return null;
		} else {
			return new FCPInsertErrorMessage(donemsg);
		}
	}
	
	public int SlotInsert(File data, String basekey, int minslot, String suffix) {
		int slot = minslot;
		boolean carryon = true;
		FileInputStream fis;
		while (carryon) {
			System.out.println("trying slotinsert to "+basekey+"-"+slot+suffix);
			
			try {
				fis = new FileInputStream(data);
			} catch (FileNotFoundException fnfe) {
				return -1;
			}
			
			FCPInsertErrorMessage emsg;
			try {
				emsg = this.put(fis, basekey+"-"+slot+suffix);
			} catch (FCPBadFileException bfe) {
				return -1;
			}
			if (emsg == null) {
				System.out.println("insert of "+basekey+"-"+slot+suffix+" successful");
				return slot;
			} else if (emsg.errorcode == FCPInsertErrorMessage.COLLISION) {
				slot++;
				System.out.println("collision");
			} else {
				System.out.println("nope - error code is "+emsg.errorcode);
				// try again later
				return -1;
			}
		}
		return -1;
	}
	
	public int SlotInsert(byte[] data, String basekey, int minslot, String suffix) {
		int slot = minslot;
		boolean carryon = true;
		ByteArrayInputStream bis;
		while (carryon) {
			System.out.println("trying slotinsert to "+basekey+"-"+slot+suffix);
			
			bis = new ByteArrayInputStream(data);
			
			FCPInsertErrorMessage emsg;
			try {
				emsg = this.put(bis, basekey+"-"+slot+suffix);
			} catch (FCPBadFileException bfe) {
				return -1;
			}
			if (emsg == null) {
				System.out.println("insert of "+basekey+"-"+slot+suffix+" successful");
				return slot;
			} else if (emsg.errorcode == FCPInsertErrorMessage.COLLISION) {
				slot++;
				System.out.println("collision");
			} else {
				System.out.println("nope - error code is "+emsg.errorcode);
				// try again later
				return -1;
			}
		}
		return -1;
	}
	
	public void requestStatus(FCPMessage msg) {
		
	}
	
	public void requestFinished(FCPMessage msg) {
		synchronized (this) {
			this.donemsg = msg;
			this.notifyAll();
		}
	}
}
