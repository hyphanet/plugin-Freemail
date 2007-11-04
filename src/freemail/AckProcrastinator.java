/*
 * AckProcrastinator.java
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

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.lang.InterruptedException;
import java.util.Random;
import java.security.SecureRandom;

import freemail.utils.PropsFile;
import freemail.fcp.HighLevelFCPClient;
import freemail.fcp.FCPBadFileException;
import freemail.fcp.FCPInsertErrorMessage;
import freemail.fcp.ConnectionTerminatedException;

/** Takes simple pieces of data to insert to keys and inserts them at some point
 * randomly within a given time frame in order to disguise the time at which messages
 * were received. This is by no means infalliable, and will only work effectively if
 * Freemail is run more or less permanantly.
 */
public class AckProcrastinator implements Runnable {
	/**
	 * Whether the thread this service runs in should stop.
	 */
	protected volatile boolean stopping = false;
	
	private Thread runThread = null; ///< The thread in which the run method is executing

	private static final long MAX_DELAY = 12 * 60 * 60 * 1000;
	private static final int RANDOM_ACK_SIZE = 512;
	
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
		runThread = Thread.currentThread();
		while (!stopping) {
			File[] acks = getAckDir().listFiles();
			
			int i;
			for (i = 0; i < acks.length; i++) {
				PropsFile ack = new PropsFile(acks[i]);
				
				String s_it  = ack.get("nominalInsertTime");
				String key = ack.get("key");
				String s_data = ack.get("data");
				if (s_it == null || key == null) {
					acks[i].delete();
					continue;
				}
				byte[] data;
				if (s_data == null) {
					SecureRandom rnd = new SecureRandom();
					
					data = new byte[RANDOM_ACK_SIZE];
					rnd.nextBytes(data);
				} else {
					data = s_data.getBytes();
				}
				long instime = Long.parseLong(s_it);
				
				if (instime < System.currentTimeMillis()) {
					HighLevelFCPClient fcpcli = new HighLevelFCPClient();
					
					ByteArrayInputStream bis = new ByteArrayInputStream(data);
					
					System.out.println("Inserting ack to "+key);
					try {
						FCPInsertErrorMessage err = fcpcli.put(bis, key);
						if (err == null) {
							acks[i].delete();
							System.out.println("ACK insertion to "+key+" sucessful");
						} else if (err.errorcode == FCPInsertErrorMessage.COLLISION) {
							acks[i].delete();
							System.out.println("ACK insertion to "+key+" sucessful");
						}
					} catch (FCPBadFileException bfe) {
						// won't occur
					} catch (ConnectionTerminatedException cte) {
						return;
					}
				}
			}
			
			if (!stopping) {
				try {
					Thread.sleep(2 * 60 * 1000);
				} catch (InterruptedException ie) {
				}
			}
		}
	}
	
	/**
	 * Terminate the run method
	 */
	public void kill() {
		stopping = true;
		if (runThread != null) {
			runThread.interrupt();
		}
	}

	/** As put(String key, String data), but insert random data
	 */
	public static synchronized void put(String key) {
		put(key, null);
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
			 if (data != null)
				 ackfile.put("data", data);
			 ackfile.put("by", Long.toString(by));
			 
			 long insertTime = System.currentTimeMillis();
			 
			 insertTime += rnd.nextFloat() * (by - insertTime);
			 
			 ackfile.put("nominalInsertTime", Long.toString(insertTime));
		} catch (IOException ioe) {
			System.out.println("IO Error whilst trying to schedule ACK for insertion! ACK will not be inserted!");
			ioe.printStackTrace();
		}
		
	}
}
