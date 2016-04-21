package com.namespace.security;

import org.pac4j.core.authorization.Authorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.UserProfile;

/**
 * Created by Aaron on 20/04/2016.
 */
public class AccountAuthorizer implements Authorizer{
    @Override
    public boolean isAuthorized(WebContext context, UserProfile profile) {
        return false;
    }
}
