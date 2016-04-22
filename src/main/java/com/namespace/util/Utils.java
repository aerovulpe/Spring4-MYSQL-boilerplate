package com.namespace.util;

import org.pac4j.core.profile.CommonProfile;

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
}
