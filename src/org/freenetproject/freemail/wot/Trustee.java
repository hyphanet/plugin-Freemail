package org.freenetproject.freemail.wot;

public class Trustee extends Identity {

    private final Byte trustValue;

    private final String comment;

    public Trustee(String identityId, String requestURI, String nickname, Byte trustValue, String comment) {
        super(identityId, requestURI, nickname);

        this.trustValue = trustValue;
        this.comment = comment;
    }

    public Byte getTrustValue() {
        return trustValue;
    }

    public String getComment() {
        return comment;
    }
}
