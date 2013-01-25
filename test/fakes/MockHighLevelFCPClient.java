/*
 * MockHighLevelFCPClient.java
 * This file is part of Freemail
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

package fakes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.freenetproject.freemail.fcp.ConnectionTerminatedException;
import org.freenetproject.freemail.fcp.FCPBadFileException;
import org.freenetproject.freemail.fcp.FCPException;
import org.freenetproject.freemail.fcp.FCPFetchException;
import org.freenetproject.freemail.fcp.FCPMessage;
import org.freenetproject.freemail.fcp.FCPPutFailedException;
import org.freenetproject.freemail.fcp.HighLevelFCPClient;
import org.freenetproject.freemail.fcp.SSKKeyPair;
import org.freenetproject.freemail.utils.Logger;

public class MockHighLevelFCPClient extends HighLevelFCPClient {
	private final Map<String, File> fetchResults;

	/**
	 * Records the fetches that have occurred. Guarded by {@code this}.
	 */
	private final List<Fetch> fetches = new LinkedList<Fetch>();

	/**
	 * Records the put operations that have occurred.
	 */
	private final List<Insert> inserts = new LinkedList<Insert>();

	public MockHighLevelFCPClient(Map<String, File> fetchResult) {
		this.fetchResults = fetchResult;
	}

	public synchronized Fetch awaitFetch(String key, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return awaitEvent(fetches, key, timeout, unit);
	}

	public synchronized Insert awaitInsert(String key, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return awaitEvent(inserts, key, timeout, unit);
	}

	private synchronized <T extends KeyEvent> T awaitEvent(List<T> events, String key, long timeout, TimeUnit unit)
			throws InterruptedException, TimeoutException {
		final long timeoutAt;
		if(timeout >= 0) {
			timeoutAt = System.nanoTime() + TimeUnit.NANOSECONDS.convert(timeout, unit);
		} else {
			timeoutAt = Long.MAX_VALUE;
		}

		while(System.nanoTime() < timeoutAt) {
			Logger.debug(this, "Checking " + events.size() + " events");
			Iterator<T> it = events.iterator();
			while(it.hasNext()) {
				T i = it.next();
				Logger.debug(this, "Checking event " + i);
				if(key == null || i.key.equals(key)) {
					it.remove();
					return i;
				}
			}

			Logger.debug(this, "No match found, waiting (key=" + key + ")");
			wait(TimeUnit.MILLISECONDS.convert(timeoutAt - System.nanoTime(), TimeUnit.NANOSECONDS));
		}

		throw new TimeoutException();
	}

	@Override
	public synchronized File fetch(String key) throws ConnectionTerminatedException, FCPFetchException, FCPException, InterruptedException {
		Logger.debug(this, "fetch(key=" + key + ")");

		if(fetchResults == null) {
			Logger.debug(this, "fetch(): Throwing GetFailed (fetchResults is null)");
			FCPFetchException e = new FCPFetchException(new FCPMessage(0, "GetFailed"));
			fetches.add(new Fetch(key, e));
			throw e;
		}

		if(!fetchResults.containsKey(key)) {
			Logger.debug(this, "fetch(): Throwing GetFailed (no data)");
			FCPFetchException e = new FCPFetchException(new FCPMessage(0, "GetFailed"));
			fetches.add(new Fetch(key, e));
			throw e;
		}

		File result = fetchResults.get(key);

		fetches.add(new Fetch(key, result));
		notifyAll();

		Logger.debug(this, "fetch(): Returning " + result);
		return result;
	}

	@Override
	public SSKKeyPair makeSSK() throws ConnectionTerminatedException, InterruptedException {
		Logger.debug(this, "makeSSK()");

		SSKKeyPair keys = new SSKKeyPair();
		keys.privkey = "SSK@Mp8ZxuCLnBkioGfhs1TuqLdng9UVZ8~n5Q0QtiUY9WI,PuO0yMON89D~5jUgwh4pmeIxTelC-p2ieTQbfHmXBUU,AQECAAE/";
		keys.pubkey = "SSK@8y6Ites9sV6uCGpkobhSzmBS1UoHJ2O0NoIWmsoqBm0,PuO0yMON89D~5jUgwh4pmeIxTelC-p2ieTQbfHmXBUU,AQACAAE/";
		return keys;
	}

	@Override
	public synchronized FCPPutFailedException put(InputStream data, String key) throws FCPBadFileException,
	                                                                      ConnectionTerminatedException,
	                                                                      FCPException, InterruptedException {
		Logger.debug(this, "put(key=" + key + ")");

		inserts.add(new Insert(key, data));
		notifyAll();

		return null;
	}

	@Override
	public int SlotInsert(File data, String basekey, int minslot, String suffix) throws ConnectionTerminatedException,
	                                                                                    InterruptedException {
		Logger.debug(this, "SlotInsert(data=" + data
		                            + ", basekey=" + basekey
		                            + ", minslot=" + minslot
		                            + ", suffix=" + suffix + ")");

		throw new UnsupportedOperationException();
	}

	@Override
	public int slotInsert(byte[] data, String basekey, int minslot, String suffix) throws ConnectionTerminatedException,
	                                                                                      InterruptedException {
		Logger.debug(this, "slotInsert(data.length=" + data.length
		                            + ", basekey=" + basekey
		                            + ", minslot=" + minslot
		                            + ", suffix=" + suffix + ")");
		try {
			put(new ByteArrayInputStream(data), basekey + "-" + minslot);
		} catch (FCPBadFileException e) {
			throw new AssertionError();
		} catch (FCPException e) {
			throw new AssertionError();
		}
		return minslot;
	}

	@Override
	public void requestStatus(FCPMessage msg) {
		Logger.debug(this, "requestStatus(msg=" + msg + ")");
		throw new UnsupportedOperationException();
	}

	@Override
	public void requestFinished(FCPMessage msg) {
		Logger.debug(this, "requestFinished(msg=" + msg + ")");
		throw new UnsupportedOperationException();
	}

	private abstract class KeyEvent {
		public String key;
	}

	public class Fetch extends KeyEvent {
		public final File result;
		public final Exception exception;

		public Fetch(String key, File result) {
			this.key = key;
			this.result = result;
			this.exception = null;
		}

		public Fetch(String key, FCPFetchException e) {
			this.key = key;
			this.result = null;
			this.exception = e;
		}
	}

	public class Insert extends KeyEvent {
		public final byte[] data;

		public Insert(String key, InputStream data) {
			this.key = key;

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			while(true) {
				try {
					int read = data.read(buffer, 0, buffer.length);
					if(read < 0) {
						break;
					}
					baos.write(buffer, 0, read);
				} catch (IOException e) {
					throw new AssertionError();
				}
			}
			this.data = baos.toByteArray();
		}
	}
}
