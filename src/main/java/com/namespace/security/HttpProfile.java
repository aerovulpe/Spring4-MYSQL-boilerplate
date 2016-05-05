package com.namespace.security;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.Gender;

/**
 * Created by Aaron on 24/04/2016.
 */
public class HttpProfile extends CommonProfile {
    @Override
    public Gender getGender() {
        return Gender.valueOf(getAttribute("gender", String.class));
    }

    public void setIp(String ipAddress) {
        addAttribute("ip", ipAddress);
    }

    public String getIp() {
        return getAttribute("ip", String.class);
    }
}
