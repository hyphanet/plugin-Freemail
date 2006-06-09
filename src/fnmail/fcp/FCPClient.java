package fnmail.fcp;

public interface FCPClient {
	public void requestFinished(FCPMessage msg);
	
	public void requestStatus(FCPMessage msg);
}
