package com.namespace.security;

import com.namespace.model.Account;
import com.namespace.service.AccountManager;
import com.namespace.util.Utils;
import org.pac4j.core.authorization.Authorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.Gender;
import org.pac4j.http.profile.HttpProfile;
import org.pac4j.oidc.profile.OidcProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Created by Aaron on 20/04/2016.
 */
@Component
public class UserAccountAuthorizer<P extends CommonProfile> implements Authorizer<P> {
    @Autowired
    private AccountManager accountManager;
    private Logger logger = LoggerFactory.getLogger(UserAccountAuthorizer.class);

    public UserAccountAuthorizer() {
    }

    @Override
    public boolean isAuthorized(WebContext context, P profile) {
        String username = Utils.getUserName(profile);
        Account account = accountManager.getAccountByUsername(username);

        if (account == null) {
            // A user using username-password verification but hasn't signed up,
            // isn't authorized.
            if (profile instanceof HttpProfile)
                return false;

            account = new Account();
            account.setUsername(username);
            account.setEmail(profile.getEmail());
            account.setLastName(profile.getFamilyName());
            if (profile instanceof OidcProfile) {
                account.setFirstName(profile.getAttribute("given_name", String.class));
                if (profile.getAttribute("gender").equals("male"))
                    account.setGender(Gender.MALE);
                else if (profile.getAttribute("gender").equals("female"))
                    account.setGender(Gender.FEMALE);
                else
                    account.setGender(Gender.UNSPECIFIED);
                account.setLocale(profile.getAttribute("locale", String.class));
                account.setPictureUrl(profile.getAttribute("picture", String.class));
            } else {
                account.setFirstName(profile.getFirstName());
                account.setPictureUrl(profile.getPictureUrl());
                account.setGender(profile.getGender());
                account.setLocale(profile.getLocale().toLanguageTag());
                account.setLocation(profile.getLocation());
            }

            account.setRoles(new HashSet<>(profile.getRoles()));
            account.setPermissions(new HashSet<>(profile.getPermissions()));

            logger.info("Attempting to save account: " + account);
            try {
                accountManager.createNewAccount(account);
            } catch (Exception e) {
                logger.error("Could not save account!", e);
                return false;
            }
        }

        return account.hasRole(Account.ROLE_USER);
    }
}
