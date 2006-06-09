package fnmail.fcp;

public class FCPErrorMessage {
	public final int errorcode;
	public final boolean isFatal;
	
	FCPErrorMessage(FCPMessage msg) {
		String code = (String)msg.headers.get("Code");
		if (code != null)
			this.errorcode = Integer.parseInt(code);
		else
			this.errorcode = 0;
		
		String fatal = (String)msg.headers.get("Fatal");
		if (fatal != null)
			this.isFatal = (fatal.equalsIgnoreCase("true"));
		else
			this.isFatal = false;
	}
}
