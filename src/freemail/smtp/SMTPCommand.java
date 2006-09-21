/*
 * SMTPCommand.java
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

package freemail.smtp;

import java.util.Vector;

public class SMTPCommand {
	public final String command;
	public final String[] args;

	public SMTPCommand(String line) throws SMTPBadCommandException {
		boolean in_quotes = false;
		Vector tmp_args = new Vector();
		StringBuffer buf = new StringBuffer("");
		
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			
			switch (c) {
				case ' ':
					if (in_quotes) {
						buf.append(c);
					} else if (buf.length() > 0) {
						tmp_args.add(buf.toString());
						buf = new StringBuffer("");
					}
					break;
				case '"':
					if (in_quotes)
						in_quotes = false;
					else
						in_quotes = true;
					break;
				default:
					buf.append(c);
			}
		}
		if (buf.length() > 0) {
			tmp_args.add(buf.toString());
		}
		if (tmp_args.size() == 0) throw new SMTPBadCommandException();
		String tmpcmd = (String)tmp_args.remove(0);
		this.command = tmpcmd.toLowerCase();
		this.args = new String[tmp_args.size()];
		
		for (int i = 0; i < tmp_args.size(); i++) {
			this.args[i] = (String)tmp_args.get(i);
		}
	}
}
