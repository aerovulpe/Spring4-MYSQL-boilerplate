package com.namespace.controller.api.v1;

import com.namespace.controller.BaseController;
import com.namespace.model.Account;
import com.namespace.web.exception.UnAuthorizedException;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by Aaron on 10/04/2016.
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    public AccountController() {
    }

    @RequestMapping(value = "/me", method = GET)
    public Account getAccount(HttpServletRequest request, HttpServletResponse response) {
        CommonProfile profile = getProfile(request, response);
        if (profile == null) {
            throw new UnAuthorizedException();
        }
        return accountFromProfile(profile);
    }
}
