package freemail.wot;

public class Identity {
	private final String identityID;
	private final String requestURI;
	private final String nickname;

	public Identity(String identityID, String requestURI, String nickname) {
		this.identityID = identityID;
		this.requestURI = requestURI;
		this.nickname = nickname;
	}

	public String getIdentityID() {
		return identityID;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public String getNickname() {
		return nickname;
	}
}
