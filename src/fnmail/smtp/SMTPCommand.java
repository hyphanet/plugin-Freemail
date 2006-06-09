package fnmail.smtp;

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
