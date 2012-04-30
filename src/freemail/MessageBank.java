/*
 * MessageBank.java
 * This file is part of Freemail
 * Copyright (C) 2006,2008 Dave Baker
 * Copyright (C) 2008 Alexander Lehmann
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Comparator;
import java.util.Arrays;

import freemail.utils.Logger;
import freemail.utils.PropsFile;

public class MessageBank {
	private static final String MESSAGES_DIR = "inbox";
	private static final String NIDFILE = ".nextid";
	private static final String NIDTMPFILE = ".nextid-tmp";
	private static final String UIDVALIDITYFILE = ".uidvalidity";
	private static final String PROPSFILE = ".props";

	private final File dir;
	private final MessageBank topLevel;
	private final long uidValidity;

	public MessageBank(FreemailAccount account) {
		this.dir = new File(account.getAccountDir(), MESSAGES_DIR);
		
		if (!this.dir.exists()) {
			this.dir.mkdir();
		}

		//This is the top level message bank
		topLevel = null;
		this.uidValidity = 1;
	}
	
	private MessageBank(File d, MessageBank topLevel) {
		this.dir = d;
		this.topLevel = topLevel;

		//Read uidvalidity from propsfile or assign a new value
		PropsFile props = PropsFile.createPropsFile(new File(dir, PROPSFILE));
		String s = props.get("uidvalidity");
		long uid;
		if(s == null) {
			//Assign a new value
			uid = getNewUidValidity();
			Logger.minor(MessageBank.class, "Assigning uidvalidity " + uid + " to " + dir);
		} else {
			try {
				uid = Long.parseLong(s);
				Logger.minor(MessageBank.class, "Read uidvalidity " + uid + " for " + dir);

				if(uid >= 0x100000000l || uid < 0) {
					uid = getNewUidValidity();
					Logger.error(this, "Read illegal uid for " + dir + ", assigning value: " + uid);
				}
			} catch(NumberFormatException e) {
				uid = getNewUidValidity();
				Logger.error(this, "Illegal uidvalidity value for " + dir + ", assigning value: " + uid);
			}
		}
		props.put("uidvalidity", uid);
		uidValidity = uid;
	}
	
	public String getName() {
		return this.dir.getName();
	}
	
	public String getFolderFlagsString() {
		StringBuffer retval = new StringBuffer("(");
		
		if (this.listSubFolders().length > 0) {
			retval.append("\\HasChildren");
		} else {
			retval.append("\\HasNoChildren");
		}
		
		retval.append(")");
		return retval.toString();
	}
	
	public synchronized boolean delete() {
		File[] files = this.dir.listFiles();
		
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(".")) continue;
			if (files[i].getName().equals("..")) continue;
			
			// this method should will fail if there are directories
			// here. It should never be called if this is the case.
			if (!files[i].delete()) return false;
		}
		
		return this.dir.delete();
	}
	
	public synchronized MailMessage createMessage() {
		long newid = this.nextId();
		File newfile;
		try {
			do {
				newfile = new File(this.dir, Long.toString(newid));
				newid++;
			} while (!newfile.createNewFile());
		} catch (IOException ioe) {
			newfile = null;
		}
		
		this.writeNextId(newid);
		
		if (newfile != null) {
			MailMessage newmsg = new MailMessage(newfile,0);
			return newmsg;
		}
		
		return null;
	}
	
	public synchronized SortedMap<Integer, MailMessage> listMessages() {
		File[] files = this.dir.listFiles(new MessageFileNameFilter());

		Arrays.sort(files, new UIDComparator());

		TreeMap<Integer, MailMessage> msgs = new TreeMap<Integer, MailMessage>();

		int seq=1;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) continue;
			
			MailMessage msg = new MailMessage(files[i],seq++);
			
			msgs.put(new Integer(msg.getUID()), msg);
		}
		
		return msgs;
	}
	
	public synchronized MailMessage[] listMessagesArray() {
		File[] files = this.dir.listFiles(new MessageFileNameFilter());

		Arrays.sort(files, new UIDComparator());
		
		MailMessage[] msgs = new MailMessage[files.length];
		
		for (int i = 0; i < files.length; i++) {
			//if (files[i].getName().startsWith(".")) continue;
			
			MailMessage msg = new MailMessage(files[i],i+1);
			
			msgs[i] = msg;
		}
		
		return msgs;
	}
	
	public MessageBank getSubFolder(String name) {
		if (!name.matches("[\\w\\s_]*")) return null;
		
		File targetdir = new File(this.dir, name);
		if (!targetdir.exists()) {
			return null;
		}

		return new MessageBank(targetdir, topLevel == null ? this : topLevel);
	}
	
	public synchronized MessageBank makeSubFolder(String name) {
		if (!name.matches("[\\w\\s_]*")) return null;
		
		File targetdir = new File(this.dir, name);
		
		//Check for a ghost directory left by old versions of Freemail
		File ghostdir = new File(this.dir, "."+name);
		if (ghostdir.exists()) {
			File[] files = ghostdir.listFiles();
			for(int i = 0; i < files.length; i++) {
				files[i].delete();
			}
			ghostdir.delete();
		}
		
		if (targetdir.exists()) {
			return null;
		}
		   
		if (targetdir.mkdir()) {
			return new MessageBank(targetdir, topLevel == null ? this : topLevel);
		}
		return null;
	}
	
	public synchronized MessageBank[] listSubFolders() {
		File[] files = this.dir.listFiles();
		Vector<File> subfolders = new Vector<File>();
		
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().startsWith(".")) continue;
			
			if (files[i].isDirectory()) {
				subfolders.add(files[i]);
			}
		}
		
		MessageBank[] retval = new MessageBank[subfolders.size()];
		
		Enumeration<File> e = subfolders.elements();
		int i = 0;
		while (e.hasMoreElements()) {
			retval[i] = new MessageBank(e.nextElement(), topLevel == null ? this : topLevel);
			i++;
		}
		return retval;
	}
	
	/**
	 * Returns the 32 bit unsigned UIDVALIDITY value for this MessageBank.
	 * @return the 32 bit unsigned UIDVALIDITY value for this MessageBank
	 */
	public long getUidValidity() {
		assert ((uidValidity >= 0) && (uidValidity < 0x100000000l)) : "Uidvalidity out of bounds: " + uidValidity;
		return uidValidity;
	}

	private synchronized long nextId() {
		File nidfile = new File(this.dir, NIDFILE);
		long retval;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(nidfile));
			
			retval = Long.parseLong(br.readLine());
		
			br.close();
		} catch (IOException ioe) {
			return 1;
		} catch (NumberFormatException nfe) {
			return 1;
		}
		
		return retval;
	}
	
	private synchronized void writeNextId(long newid) {
		// write the new ID to a temporary file
		File nidfile = new File(this.dir, NIDTMPFILE);
		try {
			PrintStream ps = new PrintStream(new FileOutputStream(nidfile));
			ps.print(newid);
			ps.flush();
			ps.close();
			
			// make sure the old nextid file doesn't contain a
			// value greater than our one
			if (this.nextId() <= newid) {
				File main_nid_file = new File(this.dir, NIDFILE);
				main_nid_file.delete();
				nidfile.renameTo(main_nid_file);
			}
		} catch (IOException ioe) {
			// how to handle this?
		}
	}
	
	private long getNewUidValidity() {
		if(topLevel != null) {
			//The top level MessageBank controls the values
			return topLevel.getNewUidValidity();
		}

		long uid;
		synchronized(this) {
			//First read the next value from the UID file
			File uidFile = new File(dir, UIDVALIDITYFILE);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(uidFile));
				try {
					uid = Long.parseLong(reader.readLine());
				} finally {
					reader.close();
				}
			} catch (FileNotFoundException e) {
				//No values have been assigned yet
				uid = uidValidity + 1;
			} catch (NumberFormatException e) {
				Logger.error(this, "Uid validity file contains illegal value, starting over. This could break IMAP clients");
				uid = uidValidity + 1;
			} catch (IOException e) {
				Logger.error(this, "Caugth IOException while reading uid validity");
				return -1;
			}

			//Write the next uid to file
			PrintStream ps;
			try {
				ps = new PrintStream(new FileOutputStream(uidFile));
			} catch (FileNotFoundException e) {
				Logger.error(this, "Couldn't create the uidvalidity file");

				//Return -1, or else we would return the same value next time
				return -1;
			}
			ps.print((uid + 1) % 0x100000000l);
			ps.close();
		}

		return uid % 0x100000000l;
	}

	private static class MessageFileNameFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			if (name.startsWith(".")) return false;
			if (!name.matches("[0-9]+(,.*)?")) return false;
			return true;
		}
	}
	
	// compare to filenames by number leading up to ","
	private static class UIDComparator implements Comparator<File> {
		@Override
		public final int compare ( File a, File b ) {
			int ia=Integer.parseInt(a.getName().split(",",2)[0]);
			int ib=Integer.parseInt(b.getName().split(",",2)[0]);

			return( ia-ib );
		}
	}


}
