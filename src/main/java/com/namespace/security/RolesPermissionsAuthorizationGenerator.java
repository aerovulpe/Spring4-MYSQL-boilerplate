package com.namespace.security;

import com.namespace.model.Account;
import com.namespace.service.AccountManager;
import org.pac4j.core.authorization.AuthorizationGenerator;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Account account = accountManager.getAccountByUsername(profile.getTypedId());

        if (account == null) {
            // New user, grant default roles and permissions
            if (defaultRoles != null) {
                profile.addRoles(defaultRoles);
            }
            if (defaultPermissions != null) {
                profile.addPermissions(defaultPermissions);
            }
            logger.info("Default Roles & Permissions.");
        } else {
            logger.info("Roles & Permissions: " + account.getRoles() + account.getPermissions());
            profile.addRoles(new ArrayList<>(account.getRoles()));
            profile.addPermissions(new ArrayList<>(account.getPermissions()));
        }
    }
}
