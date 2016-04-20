package com.namespace.controller.rest;

import com.namespace.service.AccountManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Aaron on 10/04/2016.
 */
@RestController
public class RESTAccountController {
    private static final Logger logger = LoggerFactory.getLogger(RESTAccountController.class);

    @Autowired
    private AccountManager accountManager;

    public RESTAccountController() {
    }

    public RESTAccountController(AccountManager accountManager){

    }
}
