/*
 * FCPInsertErrorMessage.java
 * This file is part of Freemail, copyright (C) 2006,2008 Dave Baker
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

@SuppressWarnings("serial")
public class FCPPutFailedException extends FCPException {
	/* Caller supplied a URI we cannot use */
	public static final int INVALID_URI = 1;
	/* Failed to read from or write to a bucket; a kind of internal error */
	public static final int BUCKET_ERROR = 2;
	/* Internal error of some sort */
	public static final int INTERNAL_ERROR = 3;
	/* Downstream node was overloaded */
	public static final int REJECTED_OVERLOAD = 4;
	/* Couldn't find enough nodes to send the data to */
	public static final int ROUTE_NOT_FOUND = 5;
	/* There were fatal errors in a splitfile insert. */
	public static final int FATAL_ERRORS_IN_BLOCKS = 6;
	/* Could not insert a splitfile because a block failed too many times */
	public static final int TOO_MANY_RETRIES_IN_BLOCKS = 7;
	/* Not able to leave the node at all */
	public static final int ROUTE_REALLY_NOT_FOUND = 8;
	/* Collided with pre-existing content */
	public static final int COLLISION = 9;
	/* Cancelled by user */
	public static final int CANCELLED = 10;
	public static final int META_STRING_IN_KEY = 11;
	public static final int INVALID_BINARY_BLOB_DATA = 12;

	// we generate this error, not Freenet
	public static final int TIMEOUT = 100;

	FCPPutFailedException(FCPMessage msg) {
		super(msg);

		assert (msg.getType().equalsIgnoreCase("PutFailed")) : "Message type was " + msg.getType();
	}

	FCPPutFailedException(int code, boolean fatal) {
		super(code, fatal);
	}

	@Override
	public String toString() {
		return "FCPPutFailedException (error code " + errorcode + ", " + (isFatal ? "fatal" : "not fatal") + ")";
	}
}
