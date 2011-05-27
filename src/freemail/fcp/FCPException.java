/*
 * FCPErrorMessage.java
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

public class FCPException extends Exception {
	public final int errorcode;
	public final boolean isFatal;

	private final String codeDescription;
	private final String extraDescription;
	private final String shortCodeDescription;

	FCPException(FCPMessage msg) {
		//Read the error code
		String code = (String)msg.headers.get("Code");
		if (code != null)
			this.errorcode = Integer.parseInt(code);
		else
			this.errorcode = 0;

		//Read the fatal field
		String fatal = (String)msg.headers.get("Fatal");
		if (fatal != null)
			this.isFatal = (fatal.equalsIgnoreCase("true"));
		else
			this.isFatal = false;

		//Read the description fields
		this.codeDescription = (String)msg.headers.get("CodeDescription");
		this.extraDescription = (String)msg.headers.get("ExtraDescription");
		this.shortCodeDescription = (String)msg.headers.get("ShortCodeDescription");
	}

	FCPException(int code, boolean fatal) {
		errorcode = code;
		isFatal = fatal;

		this.codeDescription = null;
		this.extraDescription = null;
		this.shortCodeDescription = null;
	}
}
