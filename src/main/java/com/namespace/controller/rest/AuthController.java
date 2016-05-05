package com.namespace.controller.rest;

import com.namespace.controller.BaseController;
import com.namespace.init.Pac4JConfig;
import com.namespace.security.TimedJwtGenerator;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by Aaron on 26/04/2016.
 */
@RestController
public class AuthController extends BaseController {
    @RequestMapping(value = "/jwt/dba", method = GET)

    public Map jwt(HttpServletRequest request, HttpServletResponse response) {
        final CommonProfile profile = getProfile(request, response);
        final TimedJwtGenerator<CommonProfile> generator = new TimedJwtGenerator<>(Pac4JConfig.JWT_SIGNING_SECRET,
                Pac4JConfig.JWT_ENCRYPTION_SECRET);
        String token = "";
        if (profile != null) {
            token = generator.generate(profile);
        }

        Map<String, String> jwt = new HashMap<>();
        jwt.put("access_token", token);
        return jwt;
    }
}
