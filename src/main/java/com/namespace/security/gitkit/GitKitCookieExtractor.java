package com.namespace.security.gitkit;

import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.util.CommonHelper;

import java.util.Collection;

/**
 * Created by Aaron on 07/05/2016.
 */
class GitKitCookieExtractor implements CredentialsExtractor<TokenCredentials> {
    private final String cookieName;
    private final String clientName;

    GitKitCookieExtractor(final String cookieName, final String clientName) {
        this.cookieName = cookieName;
        this.clientName = clientName;
    }

    @Override
    public TokenCredentials extract(final WebContext context) throws RequiresHttpAction {
        Collection<Cookie> cookies = context.getRequestCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return new TokenCredentials(cookie.getValue(), clientName);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "cookieName", this.cookieName,
                "clientName", this.clientName);
    }
}
