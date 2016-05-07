package com.namespace.security.jwt;


import org.pac4j.core.client.DirectClientV2;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.TokenAuthenticator;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;

/**
 * Created by Aaron on 22/04/2016.
 */
public class HeaderTokenClient extends DirectClientV2<TokenCredentials, CommonProfile> {

    private String tokenName;

    public HeaderTokenClient(final String tokenName, final TokenAuthenticator tokenAuthenticator) {
        this.tokenName = tokenName;
        setAuthenticator(tokenAuthenticator);
    }

    @Override
    protected void internalInit(final WebContext context) {
        CommonHelper.assertNotBlank("tokenName", this.tokenName);
        setCredentialsExtractor(new HeaderTokenExtractor(tokenName, getName()));
        super.internalInit(context);
    }
}
