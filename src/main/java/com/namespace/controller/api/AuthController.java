package com.namespace.controller.api;

import com.namespace.controller.BaseController;
import com.namespace.util.GitKitIdentity;
import com.namespace.util.Utils;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by Aaron on 26/04/2016.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {

    @RequestMapping(value = "/token", method = GET)
    public Map jwt(@RequestParam("gtoken") String gtoken, HttpServletResponse response) throws Exception {

        CommonProfile profile = GitKitIdentity.gitKitProfileFromUser(accountManager, GitKitIdentity.getUser(gtoken),
                GitKitIdentity.userHasVerifiedEmail(gtoken), true);
        if (profile == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return Utils.getAccessToken(profile);
    }
}
