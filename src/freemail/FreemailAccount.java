package freemail;

import java.io.File;

import freemail.utils.PropsFile;

public class FreemailAccount {
	private final String username;
	private final File accdir;
	private final PropsFile accprops;
	private final MessageBank mb;
	
	FreemailAccount(String _username, File _accdir, PropsFile _accprops) {
		username = _username;
		accdir = _accdir;
		accprops = _accprops;
		mb = new MessageBank(this);
	}
	
	public String getUsername() {
		return username;
	}
	
	public File getAccountDir() {
		return accdir;
	}
	
	public PropsFile getProps() {
		return accprops;
	}
	
	public MessageBank getMessageBank() {
		return mb;
	}
}
