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
			throw new MalformedURLException("Invalid scheme - not a freenet address");
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
	 * Read a freenet URI from args and print out in parts to test
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
