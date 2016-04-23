package com.namespace.controller.rest;

import com.namespace.controller.BaseController;
import com.namespace.model.Account;
import com.namespace.service.AccountManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AccountManager accountManager;

    public RESTAccountController() {
    }

    @RequestMapping(value = "/api/account", method = GET)
    public Account getAccount(HttpServletRequest request, HttpServletResponse response) {
        Account profile = accountManager.getAccountByUsername(getUserName(request, response));
        logger.info("Happy resting, " + profile.getFirstName());
        return profile;
    }
}
