package com.namespace.security;

import com.namespace.model.Account;
import com.namespace.service.AccountManager;
import com.namespace.util.Utils;
import org.pac4j.core.authorization.AuthorizationGenerator;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.Gender;
import org.pac4j.http.profile.HttpProfile;
import org.pac4j.oidc.profile.OidcProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Aaron on 22/04/2016.
 */
@Component
public class RolesPermissionsAuthorizationGenerator<U extends CommonProfile> implements AuthorizationGenerator<U> {
    private List<String> defaultRoles;
    private List<String> defaultPermissions;

    @Autowired
    private AccountManager accountManager;
    private Logger logger = LoggerFactory.getLogger(RolesPermissionsAuthorizationGenerator.class);

    public RolesPermissionsAuthorizationGenerator() {
    }

    public void setDefaultRoles(String... defaultRoles) {
        this.defaultRoles = Arrays.asList(defaultRoles);
    }

    public void setDefaultPermissions(String... defaultPermissions) {
        this.defaultPermissions = Arrays.asList(defaultPermissions);
    }

    @Override
    public void generate(U profile) {
        String username = Utils.getUserName(profile);
        Account account = accountManager.getAccountByUsername(username);

        if (account == null) {
            // A user using username-password verification but hasn't signed up,
            // isn't authorized.
            if (profile instanceof HttpProfile)
                return;

            logger.info("Default Roles & Permissions.");
            account = new Account();
            account.setUsername(username);
            account.setEmail(profile.getEmail());
            account.setLastName(profile.getFamilyName());

            if (profile instanceof OidcProfile) {
                // OidcProfile doesn't properly implement CommonProfile for some reason :/
                profile.addAttribute("first_name", profile.getAttribute("given_name"));
                if (profile.getAttribute("gender").equals("male"))
                    profile.addAttribute("gender", Gender.MALE);
                else if (profile.getAttribute("gender").equals("female"))
                    profile.addAttribute("gender", Gender.FEMALE);
                else
                    profile.addAttribute("gender", Gender.UNSPECIFIED);
                profile.addAttribute("locale", Locale.forLanguageTag(profile.getAttribute("locale", String.class)));
                profile.addAttribute("picture_url", profile.getAttribute("picture"));
            }
            account.setFirstName(profile.getFirstName());
            account.setPictureUrl(profile.getPictureUrl());
            account.setGender(profile.getGender());
            account.setLocale(profile.getLocale().toLanguageTag());
            account.setLocation(profile.getLocation());

            if (defaultRoles != null) {
                account.setRoles(new HashSet<>(defaultRoles));
                profile.addRoles(defaultRoles);
            }
            if (defaultPermissions != null) {
                account.setPermissions(new HashSet<>(defaultPermissions));
                profile.addPermissions(defaultPermissions);
            }

            try {
                accountManager.createNewAccount(account);
            } catch (Exception e) {
                logger.error("Could not save account!", e);
            }
        } else {
            logger.info("Roles & Permissions: " + account.getRoles() + account.getPermissions());
            profile.addRoles(new ArrayList<>(account.getRoles()));
            profile.addPermissions(new ArrayList<>(account.getPermissions()));
        }
    }
}
