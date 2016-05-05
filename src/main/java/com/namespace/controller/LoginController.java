package com.namespace.controller;

import com.namespace.model.Account;
import com.namespace.service.GitKitIdentityService;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Handles requests for the application home page.
 */
@Controller
public class LoginController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private GitKitIdentityService gitKitIdentityService;

    public LoginController() {
    }

    @RequestMapping(value = "/", method = GET)
    public void home(HttpServletRequest request, HttpServletResponse response) {
        CommonProfile profile = getProfile(request, response);
        if (profile != null) {
            if (profile.getRoles().contains(Account.ROLE_ADMIN)) {
                logger.info("Welcome home, admin " + profile.getFirstName() + "!");
                serveHtmlPage("static/admin/index.html", response);
            } else {
                logger.info("Welcome home, user " + profile.getFirstName() + "!");
                serveHtmlPage("static/user/index.html", response);
            }
        } else {
            logger.info("not logged in!");
            serveHtmlPage("static/landing.html", response);
        }
    }

    @RequestMapping("/gitkit/success")
    public String gitkitSignIn(HttpServletRequest request, HttpServletResponse response) throws Exception {
        CommonProfile profile = getProfile(request, response);
        logger.info("Welcome, " + profile.getFirstName() + "!");
        return "redirect:/";
    }

    @RequestMapping("/oauth2callback")
    public void gitkitWidget(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        gitKitIdentityService.handleOauthCallback(request, response);
    }

    @RequestMapping("/email")
    public void gitkitEmail(HttpServletRequest request, HttpServletResponse response) {
        gitKitIdentityService.sendEmail(request, response);
    }
}

