package com.namespace.controller.api.v1;

import com.namespace.controller.BaseController;
import com.namespace.security.jwt.TimedJwtGenerator;
import com.namespace.service.GitKitIdentityService;
import com.namespace.web.exception.UnAuthorizedException;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by Aaron on 26/04/2016.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController {
    @Autowired
    private GitKitIdentityService gitKitIdentityService;
    @Autowired
    private TimedJwtGenerator<CommonProfile> timedJwtGenerator;

    @RequestMapping(value = "/token", method = GET)
    public Map jwt(@RequestParam("gtoken") String gtoken) {

        CommonProfile profile = gitKitIdentityService.getGitKitProfile(gtoken, true);
        if (profile == null) {
            throw new UnAuthorizedException();
        }
        return timedJwtGenerator.generateToken(profile);
    }
}
