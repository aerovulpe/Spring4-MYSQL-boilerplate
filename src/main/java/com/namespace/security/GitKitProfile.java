package com.namespace.security;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.Gender;

/**
 * Created by Aaron on 28/04/2016.
 */
public class GitKitProfile extends CommonProfile {
    @Override
    public Gender getGender() {
        return Gender.valueOf(getAttribute("gender", String.class));
    }
}
