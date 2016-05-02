package com.namespace.util;

import com.namespace.init.Pac4JConfig;
import com.namespace.model.Account;
import com.namespace.security.TimedJwtGenerator;
import org.pac4j.core.profile.CommonProfile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Aaron on 22/04/2016.
 */
public final class Utils {

    private static final TimedJwtGenerator<CommonProfile> JWT_GENERATOR =
            new TimedJwtGenerator<>(Pac4JConfig.JWT_SIGNING_SECRET,
                    Pac4JConfig.JWT_ENCRYPTION_SECRET);

    private Utils() {
    }

    public static String getUserNaturalId(CommonProfile profile) {
        return profile.getId();
    }

    public static Account accountFromProfile(CommonProfile profile) {
        Account account = new Account();
        if (profile.getAttribute("account_id") != null)
            account.setId(profile.getAttribute("account_id", Long.class));
        account.setNaturalId(Utils.getUserNaturalId(profile));
        account.setFirstName(profile.getFirstName());
        account.setLastName(profile.getFamilyName());
        account.setEmail(profile.getEmail());
        account.setPictureUrl(profile.getPictureUrl());
        account.setGender(profile.getGender());
        account.setLocale(profile.getLocale() == null ? null : profile.getLocale().toLanguageTag());
        account.setLocation(profile.getLocation());
        account.setRemembered(profile.isRemembered());
        account.setRoles(new HashSet<>(profile.getRoles()));
        account.setPermissions(new HashSet<>(profile.getPermissions()));
        return account;
    }

    public static Map<String, String> getAccessToken(CommonProfile profile) {
        Map<String, String> token;
        if (profile != null) {
            token = JWT_GENERATOR.generateToken(profile);
        } else {
            token = new HashMap<>();
            token.put("status", "unauthorized");
        }

        return token;
    }
}
