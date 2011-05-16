package freemail.utils;

import freenet.support.SimpleFieldSet;

public class SimpleFieldSetFactory {
	private final SimpleFieldSet sfs = new SimpleFieldSet(false);

	public SimpleFieldSetFactory put(String field, String value) {
		sfs.putOverwrite(field, value);
		return this;
	}

	public SimpleFieldSet create() {
		return sfs;
	}
}
