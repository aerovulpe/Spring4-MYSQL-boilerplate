package com.namespace.util;

import com.namespace.init.Pac4JConfig;
import com.namespace.security.TimedJwtGenerator;
import org.pac4j.core.profile.CommonProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aaron on 22/04/2016.
 */
public class Utils {

    private static final TimedJwtGenerator<CommonProfile> JWT_GENERATOR =
            new TimedJwtGenerator<>(Pac4JConfig.JWT_SIGNING_SECRET,
                    Pac4JConfig.JWT_ENCRYPTION_SECRET);

    private Utils() {
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
