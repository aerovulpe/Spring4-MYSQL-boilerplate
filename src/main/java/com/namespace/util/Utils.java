package com.namespace.util;

import com.namespace.model.Account;
import org.pac4j.core.profile.CommonProfile;

import java.util.HashSet;

/**
 * Created by Aaron on 22/04/2016.
 */
public final class Utils {
    private Utils() {
    }

    public static String getUserName(CommonProfile profile) {
        return profile.getUsername() == null ?
                profile.getTypedId() : profile.getUsername();
    }

    public static Account accountFromProfile(CommonProfile profile) {
        Account account = new Account();
        account.setUsername(Utils.getUserName(profile));
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
}
