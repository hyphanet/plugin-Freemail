package freemail.config;

import java.io.File;
import java.util.HashMap;

import freemail.utils.PropsFile;

/** "Probably the simplest config interface in the world"
 *
 */

public class Configurator {
	private final PropsFile props;
	private final HashMap callbacks;

	public Configurator(File f) {
		this.props = new PropsFile(f);
		this.props.setCommentPrefix("#");
		String ls = System.getProperty("line.separator");
		StringBuffer head = new StringBuffer();
		head.append("# This is the configuration file for Freemail."+ls);
		head.append("# "+ls);
		head.append("# You are free to edit this file, but if Freemail"+ls);
		head.append("# breaks as a result, you should delete this file"+ls);
		head.append("# and Freemail will reset all values to their"+ls);
		head.append("# defaults."+ls);
		head.append("# The Freemail community cannot provide support"+ls);
		head.append("# to people who have broken Freemail as a result"+ls);
		head.append("# of editing this file.");
		
		this.props.setHeader(head.toString());
		this.callbacks = new HashMap();
	}
	
	public void register(String key, ConfigClient cb, String defaultval) {
		this.callbacks.put(key, cb);
		
		String val = this.props.get(key);
		if (val == null) {
			val = defaultval;
			props.put(key, val);
		}
		cb.setConfigProp(key, val);
	}
	
	public void set(String key, String val) {
		this.props.put(key, val);
		
		ConfigClient cb = (ConfigClient)this.callbacks.get(key);
		if (cb == null) return;
		cb.setConfigProp(key, val);
	}
}
