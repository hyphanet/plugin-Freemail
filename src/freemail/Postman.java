package freemail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.IOException;

/** A postman is any class that delivers mail to an inbox. Simple,
 *  if not politically correct.
 */
public abstract class Postman {
	protected void storeMessage(BufferedReader brdr, MessageBank mb) throws IOException {
		MailMessage newmsg = mb.createMessage();
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
		
		// add our own headers first
		// recieved and date
		newmsg.addHeader("Received", "(Freemail); "+sdf.format(new Date()));
		
		newmsg.readHeaders(brdr);
		
		PrintStream ps = newmsg.writeHeadersAndGetStream();
		
		String line;
		while ( (line = brdr.readLine()) != null) {
			ps.println(line);
		}
		
		newmsg.commit();
		brdr.close();
	}
}
