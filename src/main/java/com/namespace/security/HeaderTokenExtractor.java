package com.namespace.security;

import org.pac4j.core.context.WebContext;
import org.pac4j.http.credentials.TokenCredentials;
import org.pac4j.http.credentials.extractor.Extractor;

/**
 * Created by Aaron on 22/04/2016.
 */
class HeaderTokenExtractor implements Extractor<TokenCredentials> {
    private final String tokenName;
    private final String clientName;


    HeaderTokenExtractor(String tokenName, String clientName) {
        this.tokenName = tokenName;
        this.clientName = clientName;
    }

    @Override
    public TokenCredentials extract(WebContext context) {
        final String value = context.getRequestHeader(this.tokenName);

        if (value == null) {
            return null;
        }

        return new TokenCredentials(value, clientName);
    }
}
