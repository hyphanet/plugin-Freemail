package freemail.fcp;

public class NoNodeConnectionException extends Exception {
	static final long serialVersionUID = -1;

	NoNodeConnectionException() {
		super();
	}

	NoNodeConnectionException(String s) {
		super(s);
	}
}
