/*
 * FreenetURI.java
 * This file is part of Freemail, copyright (C) 2006 Dave Baker
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

package freemail;

import java.net.MalformedURLException;

/*
 * Represents a Freenet URI
 * If this gets complicated, look at using Freenet's own class of the same name (has dependancies though)
 */
public class FreenetURI {
	//Regular expressions for matching SSKs and USKs
	private static final String BASE64_REGEXP = "\\S";
	private static final String SSK_HASH_REGEX = BASE64_REGEXP + "{42,44}";
	private static final String SSK_KEY_REGEX = SSK_HASH_REGEX;
	private static final String SSK_EXTRA_REGEX = BASE64_REGEXP + "{7}";
	private static final String SSK_REGEX = "SSK@" + SSK_HASH_REGEX + "," + SSK_KEY_REGEX + "," + SSK_EXTRA_REGEX;
	private static final String USK_REGEX = "USK@" + SSK_HASH_REGEX + "," + SSK_KEY_REGEX + "," + SSK_EXTRA_REGEX;
	private static final String USK_SITENAME_REGEX = "\\w+";
	private static final String USK_EDITION_REGEX = "-?[0-9]+";

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

	/**
	 * Returns {@code true} if {@code key} is a valid SSK key and {@code false} otherwise.
	 * @param key the key that should be checked
	 * @return {@code true} if {@code key} is a valid SSK key
	 */
	public static boolean checkSSK(String key) {
		//Valid SSK, possibly with sitename and filename
		if(key.matches("^" + SSK_REGEX + "/.*$")) return true;

		//Valid SSK without trailing /
		if(key.matches("^" + SSK_REGEX + "$")) return true;

		return false;
	}

	/**
	 * Returns {@code true} if {@code key} is a valid USK key and {@code false} otherwise.
	 * @param key the key that should be checked
	 * @return {@code true} if {@code key} is a valid USK key
	 */
	public static boolean checkUSK(String key) {
		//Valid USK with sitename, edition and filename
		if(key.matches("^" + USK_REGEX + "/" + USK_SITENAME_REGEX + "/" + USK_EDITION_REGEX + "/.*$")) return true;

		//Valid USK with sitename, edition (optional trailing /)
		if(key.matches("^" + USK_REGEX + "/" + USK_SITENAME_REGEX + "/" + USK_EDITION_REGEX + "/{0,1}$")) return true;

		//Valid USK with sitename but no edition (optional trailing /)
		if(key.matches("^" + USK_REGEX + "/" + USK_SITENAME_REGEX + "/{0,1}$")) return true;

		//Valid USK without sitename (optional trailing /)
		if(key.matches("^" + USK_REGEX + "/{0,1}$")) return true;

		return false;
	}

	/**
	 * Returns {@code true} if {@code hash} is a valid hash from an SSK key and {@code false}
	 * otherwise. Since the hash is also used as the identity ID by WoT this can be used to verify
	 * identity ids as well.
	 * @param hash the hash that should be checked
	 * @return {@code true} if {@code key} is a valid SSK hash
	 */
	public static boolean checkSSKHash(String hash) {
		return hash.matches("^" + SSK_HASH_REGEX + "$");
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
