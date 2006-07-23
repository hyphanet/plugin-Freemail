package freemail;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.lang.InterruptedException;
import java.util.Random;

import freemail.utils.PropsFile;
import freemail.fcp.HighLevelFCPClient;
import freemail.fcp.FCPBadFileException;
import freemail.fcp.FCPInsertErrorMessage;

/** Takes simple pieces of data to insert to keys and inserts them at some point
 * randomly within a given time frame in order to disguise the time at which messages
 * were received. This is by no means infalliable, and will only work effectively if
 * Freemail is run more or less permanantly.
 */
public class AckProcrastinator implements Runnable {
	private static final long MAX_DELAY = 12 * 60 * 60 * 1000;
	
	private static File ackdir;
	private static Random rnd;
	
	public AckProcrastinator() {
		rnd = new Random();
		File ackdir = getAckDir();
		if (!ackdir.exists()) {
			ackdir.mkdir();
		}
	}
	
	private static File getAckDir() {
		return AckProcrastinator.ackdir;
	}
	
	public static void setAckDir(File dir) {
		AckProcrastinator.ackdir = dir;
		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	public void run() {
		while (true) {
			File[] acks = getAckDir().listFiles();
			
			int i;
			for (i = 0; i < acks.length; i++) {
				PropsFile ack = new PropsFile(acks[i]);
				
				String s_it  = ack.get("nominalInsertTime");
				String key = ack.get("key");
				String data = ack.get("data");
				if (s_it == null || key == null || data == null) {
					acks[i].delete();
					continue;
				}
				long instime = Long.parseLong(s_it);
				
				if (instime < System.currentTimeMillis()) {
					HighLevelFCPClient fcpcli = new HighLevelFCPClient();
					
					ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes());
					
					System.out.println("Inserting ack to "+key);
					try {
						FCPInsertErrorMessage err = fcpcli.put(bis, key);
						if (err == null) {
							acks[i].delete();
						} else if (err.errorcode == FCPInsertErrorMessage.COLLISION) {
							acks[i].delete();
						}
					} catch (FCPBadFileException bfe) {
						// won't occur
					}
				}
			}
			
			try {
				Thread.sleep(2 * 60 * 1000);
			} catch (InterruptedException ie) {
			}
		}
	}
	
	/** Insert some data at some random point in the future, but ideally before
	 * 'by' (in milliseconds).
	 */
	public static synchronized void put(String key, String data) {
		// this could be a parameter if desired in the future
		long by = System.currentTimeMillis() + MAX_DELAY;
		
		try {
			PropsFile ackfile= new PropsFile(File.createTempFile("delayed-ack", "", getAckDir()));
			 
			 ackfile.put("key", key);
			 ackfile.put("data", data);
			 ackfile.put("by", Long.toString(by));
			 
			 long insertTime = System.currentTimeMillis();
			 
			 insertTime += rnd.nextFloat() * by;
			 
			 ackfile.put("nominalInsertTime", Long.toString(insertTime));
		} catch (IOException ioe) {
			System.out.println("IO Error whilst trying to schedule ACK for insertion! ACK will not be inserted!");
			ioe.printStackTrace();
		}
		
	}
}
