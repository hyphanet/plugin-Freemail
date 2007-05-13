/*
 * FreenetURI.java
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

import java.net.MalformedURLException;

/*
 * Represents a Freenet URI
 * If this gets complicated, look at using Freenet's own class of the same name (has dependancies though)
 */
public class FreenetURI {
	private String keytype;
	private String keybody;
	private String suffix; // this includes USK versions etc for now

	public FreenetURI(String uri) throws MalformedURLException {
		String[] parts = uri.split(":", 2);
		
		if (parts.length == 2 && !parts[0].equals("freenet")) {
			throw new MalformedURLException("Invalid scheme - not a Freenet address");
		} else if (parts.length == 2) {
			uri = parts[1];
		}
		
		// now split on the '@'
		parts = uri.split("@", 2);
		
		if (parts.length < 2) {
			this.keytype  = "KSK";
		} else {
			this.keytype = parts[0];
			uri = parts[1];
		}
		
		// finally, separate the body from the metastrings
		parts = uri.split("/", 2);
		
		if (parts.length < 2) {
			this.keybody = uri;
			this.suffix = null;
		} else {
			this.keybody = parts[0];
			this.suffix = parts[1];
		}
	}
	
	public String getKeyType() {
		return this.keytype;
	}
	
	public String getKeyBody() {
		return this.keybody;
	}
	
	public String getSuffix() {
		return this.suffix;
	}
	
	/*
	 * Read a Freenet URI from args and print out in parts to test
	 */
	public static void main(String args[]) {
		FreenetURI uri;
		try {
			uri = new FreenetURI(args[0]);
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
			return;
		}
		
		System.out.println("Key type: "+uri.getKeyType());
		System.out.println("Key body: "+uri.getKeyBody());
		System.out.println("Suffix: "+uri.getSuffix());
	}
}
