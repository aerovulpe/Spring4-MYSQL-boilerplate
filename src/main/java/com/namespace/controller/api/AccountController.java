package com.namespace.controller.api;

import com.namespace.controller.BaseController;
import com.namespace.model.Account;
import com.namespace.util.Utils;
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
@RequestMapping("/api/accounts")
public class AccountController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    public AccountController() {
    }

    @RequestMapping(value = "/me", method = GET)
    public Account getAccount(HttpServletRequest request, HttpServletResponse response) {
        return Utils.accountFromProfile(getProfile(request, response));
    }
}
