/*
 * HighLevelFCPClient.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007 Alexander Lehmann
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

package freemail.fcp;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

import freemail.Freemail;
import freemail.utils.Logger;

public class HighLevelFCPClient implements FCPClient {
	private static final int FCP_TOO_MANY_PATH_COMPONENTS = 11;
	private static final int FCP_PERMANANT_REDIRECT = 27;
	// wait 10 minutes before giving up on inserts
	private static final int PUT_TIMEOUT = 10 * 60 * 1000;

	private FCPConnection conn;

	private FCPMessage donemsg;
	private final Object donemsgLock = new Object();
	
	public HighLevelFCPClient() {
		this.conn = Freemail.getFCPConnection();
	}
	
	// It's up to the client to delete this File once they're
	// done with it
	public synchronized File fetch(String key) throws ConnectionTerminatedException,
	                                                  FCPFetchException, FCPException,
	                                                  InterruptedException {
		FCPMessage msg = this.conn.getMessage("ClientGet");
		msg.headers.put("URI", key);
		msg.headers.put("ReturnType", "direct");
		msg.headers.put("Persistence", "connection");

		FCPMessage reply;
		synchronized(donemsgLock) {
			assert (this.donemsg == null);
			this.donemsg = null;

			while (true) {
				try {
					this.conn.doRequest(this, msg);
					break;
				} catch (NoNodeConnectionException nnce) {
					Logger.error(this,"Warning - no connection to node. Waiting...");
					Thread.sleep(10000);
				} catch (FCPBadFileException bfe) {
					// won't be thrown since this is a get,
					// but keep the compiler happy
				}
			}

			while (this.donemsg == null) {
				try {
					donemsgLock.wait();
				} catch (InterruptedException ie) {
					Logger.debug(this, "HighLevelFCPClient interrupted in fetch, stopping");
					conn.cancelRequest(msg);
					throw ie;
				}
			}
			reply = this.donemsg;
			this.donemsg = null;
		}
		
		if (reply.getType().equalsIgnoreCase("AllData")) {
			return reply.getData();
		} else if (reply.getType().equalsIgnoreCase("GetFailed")) {
			String s_code = reply.headers.get("Code");
			if (s_code == null) return null;
			int code = Integer.parseInt(s_code);
			if (code == FCP_PERMANANT_REDIRECT || code == FCP_TOO_MANY_PATH_COMPONENTS) {
				String newuri = reply.headers.get("RedirectURI");
				if (newuri == null) return null;
				return this.fetch(newuri);
			}
			throw new FCPFetchException(reply);
		} else {
			throw FCPException.create(reply);
		}
	}
	
	public synchronized SSKKeyPair makeSSK() throws ConnectionTerminatedException,
	                                                InterruptedException {
		FCPMessage msg = this.conn.getMessage("GenerateSSK");

		FCPMessage reply;
		synchronized(donemsgLock) {
			assert (this.donemsg == null);
			this.donemsg = null;

			while (true) {
				try {
					this.conn.doRequest(this, msg);
					break;
				} catch (NoNodeConnectionException nnce) {
					Logger.error(this,"Warning - no connection to node. Waiting...");
					Thread.sleep(5000);
				} catch (FCPBadFileException bfe) {
					// won't be thrown since no data
				}
			}

			while (this.donemsg == null) {
				try {
					donemsgLock.wait();
				} catch (InterruptedException ie) {
					Logger.debug(this, "HighLevelFCPClient interrupted in makeSSK, stopping");
					conn.cancelRequest(msg);
					throw ie;
				}
			}

			reply = this.donemsg;
			this.donemsg = null;
		}
		
		if (reply.getType().equalsIgnoreCase("SSKKeypair")) {
			SSKKeyPair retval = new SSKKeyPair();
			
			retval.privkey = reply.headers.get("InsertURI");
			retval.pubkey = reply.headers.get("RequestURI");
			return retval;
		} else {
			return null;
		}
	}
	
	public synchronized FCPPutFailedException put(InputStream data, String key) throws FCPBadFileException,
	                                                                                   ConnectionTerminatedException,
	                                                                                   FCPException,
	                                                                                   InterruptedException {
		FCPMessage msg = this.conn.getMessage("ClientPut");
		msg.headers.put("URI", key);
		msg.headers.put("Persistence", "connection");
		msg.setData(data);
		
		FCPMessage reply;
		synchronized(donemsgLock) {
			assert (this.donemsg == null);
			this.donemsg = null;

			long startedAt = 0;
			while (true) {
				try {
					this.conn.doRequest(this, msg);
					startedAt = System.currentTimeMillis();
					break;
				} catch (NoNodeConnectionException nnce) {
					Logger.error(this,"Warning - no connection to node. Waiting...");
					Thread.sleep(5000);
				}
			}

			while (this.donemsg == null) {
				if (System.currentTimeMillis() > startedAt + PUT_TIMEOUT) {
					Logger.error(this, "Put timed out after "+PUT_TIMEOUT+"ms. That's not good!");
					// 'cancel' the request, otherwise we'll leak memory
					this.conn.cancelRequest(msg);

					return new FCPPutFailedException(FCPPutFailedException.TIMEOUT, false);
				}
				try {
					donemsgLock.wait(30000);
				} catch (InterruptedException ie) {
					Logger.debug(this, "HighLevelFCPClient interrupted in put, stopping");
					conn.cancelRequest(msg);
					throw ie;
				}
			}

			reply = this.donemsg;
			this.donemsg = null;
		}
		
		if (reply.getType().equalsIgnoreCase("PutSuccessful")) {
			return null;
		} else if(reply.getType().equalsIgnoreCase("PutFailed")) {
			return new FCPPutFailedException(reply);
		} else {
			throw FCPException.create(reply);
		}
	}
	
	public int SlotInsert(File data, String basekey, int minslot, String suffix) throws ConnectionTerminatedException,
	                                                                                    InterruptedException {
		int slot = minslot;
		boolean carryon = true;
		FileInputStream fis;
		if(basekey.startsWith("USK@")) {
			basekey = basekey.replace("USK@", "SSK@");

			if(basekey.charAt(basekey.length() - 1) == '/') {
				basekey = basekey.substring(0, basekey.length() - 1);
			}
		}
		while (carryon) {
			Logger.normal(this,"trying slotinsert to "+basekey+"-"+slot+suffix);
			
			try {
				fis = new FileInputStream(data);
			} catch (FileNotFoundException fnfe) {
				return -1;
			}
			
			FCPPutFailedException emsg;
			try {
				emsg = this.put(fis, basekey+"-"+slot+suffix);
			} catch (FCPBadFileException bfe) {
				return -1;
			} catch (FCPException e) {
				Logger.error(this, "Unknown error while doing slotinsert: " + e);
				return -1;
			}
			if (emsg == null) {
				Logger.normal(this,"insert of "+basekey+"-"+slot+suffix+" successful");
				return slot;
			} else if (emsg.errorcode == FCPPutFailedException.COLLISION) {
				slot++;
				Logger.normal(this,"collision");
			} else {
				Logger.error(this,"Slot insert failed, error code is "+emsg.errorcode);
				// try again later
				return -1;
			}
		}
		return -1;
	}
	
	public int slotInsert(byte[] data, String basekey, int minslot, String suffix) throws ConnectionTerminatedException,
	                                                                                      InterruptedException {
		int slot = minslot;
		boolean carryon = true;
		ByteArrayInputStream bis;
		if(basekey.startsWith("USK@")) {
			basekey = basekey.replace("USK@", "SSK@");

			if(basekey.charAt(basekey.length() - 1) == '/') {
				basekey = basekey.substring(0, basekey.length() - 1);
			}
		}
		while (carryon) {
			Logger.normal(this,"trying slotinsert to "+basekey+"-"+slot+suffix);
			
			bis = new ByteArrayInputStream(data);
			
			FCPPutFailedException emsg;
			try {
				emsg = this.put(bis, basekey+"-"+slot+suffix);
			} catch (FCPBadFileException bfe) {
				return -1;
			} catch (FCPException e) {
				Logger.error(this, "Unknown error while doing slotinsert: " + e);
				return -1;
			}
			if (emsg == null) {
				Logger.normal(this,"insert of "+basekey+"-"+slot+suffix+" successful");
				return slot;
			} else if (emsg.errorcode == FCPPutFailedException.COLLISION) {
				slot++;
				Logger.normal(this,"collision");
			} else {
				Logger.error(this,"Slot insert failed, error code is "+emsg.errorcode);
				// try again later
				return -1;
			}
		}
		return -1;
	}
	
	@Override
	public void requestStatus(FCPMessage msg) {
		
	}
	
	@Override
	public void requestFinished(FCPMessage msg) {
		synchronized (donemsgLock) {
			assert (donemsg == null);
			this.donemsg = msg;
			donemsgLock.notifyAll();
		}
	}
}
