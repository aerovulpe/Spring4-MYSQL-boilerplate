package com.namespace.security;

import org.pac4j.core.client.ClientType;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.client.direct.DirectHttpClient;
import org.pac4j.http.credentials.TokenCredentials;
import org.pac4j.http.credentials.authenticator.TokenAuthenticator;

/**
 * Created by Aaron on 22/04/2016.
 */
public class HeaderTokenClient extends DirectHttpClient<TokenCredentials> {

    private String tokenName = "";

    private HeaderTokenClient() {
    }

    public HeaderTokenClient(final String tokenName, final TokenAuthenticator tokenAuthenticator) {
        this.tokenName = tokenName;
        setAuthenticator(tokenAuthenticator);
    }

    @Override
    protected void internalInit(final WebContext context) {
        extractor = new HeaderTokenExtractor(this.tokenName, getName());
        super.internalInit(context);
        CommonHelper.assertNotBlank("tokenName", this.tokenName);
    }

    @Override
    protected HeaderTokenClient newClient() {
        final HeaderTokenClient newClient = new HeaderTokenClient();
        newClient.setTokenName(this.tokenName);
        return newClient;
    }

    @Override
    public ClientType getClientType() {
        return ClientType.PARAMETER_BASED;
    }

    private void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }
}
