package freemail.fcp;

public class FCPProtocolException extends FCPException {
	FCPProtocolException(FCPMessage msg) {
		super(msg);

		assert (msg.getType().equalsIgnoreCase("ProtocolError")) : "Message type was " + msg.getType();
	}

	public String toString() {
		return "FCP Protocol Error (error code " + errorcode + ": " + codeDescription + ")";
	}
}
