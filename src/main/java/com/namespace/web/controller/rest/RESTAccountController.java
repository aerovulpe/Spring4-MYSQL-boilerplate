package com.namespace.web.controller.rest;

import com.namespace.web.controller.BaseController;
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
public class RESTAccountController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(RESTAccountController.class);

    public RESTAccountController() {
    }

    @RequestMapping(value = "/api/account", method = GET)
    public Account getAccount(HttpServletRequest request, HttpServletResponse response) {
        Account account = Utils.accountFromProfile(getProfile(request, response));
        logger.info("Happy resting, " + account.getFirstName());
        return account;
    }
}
