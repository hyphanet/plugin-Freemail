package freemail.fcp;

public class ConnectionTerminatedException extends Exception {
	static final long serialVersionUID = -1;
	
	ConnectionTerminatedException() {
		super();
	}

	ConnectionTerminatedException(String s) {
		super(s);
	}
}
