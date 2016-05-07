package com.namespace.security.gitkit;

import org.pac4j.core.client.DirectClientV2;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.TokenAuthenticator;
import org.pac4j.core.util.CommonHelper;

/**
 * Created by Aaron on 05/05/2016.
 */
public class GitkitClient extends DirectClientV2<TokenCredentials, GitKitProfile> {
    private String tokenName;

    public GitkitClient(final String tokenName, final TokenAuthenticator tokenAuthenticator) {
        this.tokenName = tokenName;
        setAuthenticator(tokenAuthenticator);
    }

    @Override
    protected void internalInit(final WebContext context) {
        CommonHelper.assertNotBlank("tokenName", this.tokenName);
        setCredentialsExtractor(new GitKitCookieExtractor(tokenName, getName()));
        super.internalInit(context);
    }
}
