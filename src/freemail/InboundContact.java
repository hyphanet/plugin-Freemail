package freemail;

import java.io.File;

import freemail.FreenetURI;
import freemail.utils.PropsFile;

public class InboundContact {
	private static final String IBCT_PROPSFILE = "props";
	private File ibct_dir;
	private PropsFile ibct_props;
	
	public InboundContact(File contact_dir, FreenetURI mailsite) {
		this(contact_dir, mailsite.getKeyBody());
	}

	private InboundContact(File contact_dir, String keybody) {
		this.ibct_dir = new File(contact_dir, keybody);
		
		if (!this.ibct_dir.exists()) {
			this.ibct_dir.mkdir();
		}
		
		this.ibct_props = new PropsFile(new File(this.ibct_dir, IBCT_PROPSFILE));
	}
	
	public void setProp(String key, String val) {
		this.ibct_props.put(key, val);
	}
}
