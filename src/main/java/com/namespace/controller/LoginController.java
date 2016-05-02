package com.namespace.controller;

import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitUser;
import com.namespace.model.Account;
import com.namespace.security.GitKitProfile;
import com.namespace.service.AccountManager;
import com.namespace.util.Utils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Handles requests for the application home page.
 */
@Controller
public class LoginController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    AccountManager accountManager;

    public LoginController() {
    }

    @RequestMapping(value = "/", method = GET)
    public void home(HttpServletRequest request, HttpServletResponse response) {
        CommonProfile profile = getProfile(request, response);
        if (profile != null) {
            if (profile.getRoles().contains(Account.ROLE_ADMIN)) {
                logger.info("Welcome home admin, " + profile.getFirstName() + "!");
                serveHtmlPage("static/admin/index.html", response);
            } else {
                logger.info("Welcome home user, " + profile.getFirstName() + "!");
                serveHtmlPage("static/user/index.html", response);
            }
        } else {
            logger.info("not logged in!");
            serveHtmlPage("static/landing.html", response);
        }
    }

    @RequestMapping("/gitkit/success")
    public String gitkitSignIn(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GitkitUser gitkitUser;
        GitkitClient gitkitClient = GitkitClient.createFromJson(getClass().getClassLoader()
                .getResource("gitkit-server-config.json").getPath().substring(1));


        gitkitUser = gitkitClient.validateTokenInRequest(request);
        String userInfo;
        if (gitkitUser != null) {
            userInfo = "Welcome back!<br><br> Email: " + gitkitUser.getEmail() + "<br> Id: "
                    + gitkitUser.getLocalId() + "<br> Provider: " + gitkitUser.getCurrentProvider();
            logger.info(userInfo);

            ProfileManager<GitKitProfile> gitKitProfileProfileManager =
                    new ProfileManager<>(new J2EContext(request, response));
            Account account = accountManager.getAccountByNaturalId(gitkitUser.getLocalId());
            if (account == null) {
                logger.info("New account");
                String[] names = gitkitUser.getName().split(" ");
                String lastName = names.length > 1 ? names[names.length - 1] : "";
                account = new Account(gitkitUser.getLocalId(), null, names[0], lastName, gitkitUser.getEmail());
                account.addRole(Account.ROLE_USER);
                account.addPermission(Account.PERMISSION_ENABLED);
                accountManager.createNewAccount(account);
            }

            logger.info("Account: " + account.toString());
            final GitKitProfile profile = new GitKitProfile();
            profile.setId(account.getNaturalId());
            profile.addAttribute("account_id", account.getId());
            profile.addAttribute("email", account.getEmail());
            profile.addAttribute("first_name", account.getFirstName());
            profile.addAttribute("family_name", account.getLastName());
            profile.addAttribute("name", account.getFirstName() + " " + account.getLastName());
            profile.addAttribute("display_name", account.getFirstName());
            profile.addAttribute("gender", account.getGender());
            profile.addAttribute("locale", account.getLocale());
            profile.addAttribute("picture_url", account.getPictureUrl());
            profile.addAttribute("location", account.getLocation());
            profile.setRemembered(account.isRemembered());
            profile.addRoles(new ArrayList<>(account.getRoles()));
            profile.addPermissions(new ArrayList<>(account.getPermissions()));

            gitKitProfileProfileManager.save(true, profile, false);

            response.addCookie(new Cookie("access_token", Utils.getAccessToken(profile).get("access_token")));
        }

        return "redirect:/";
    }

    @RequestMapping("/oauth2callback")
    public void gitkitWidget(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        returnGitkitWidget("static/gitkit-widget.html", request, response);
    }

    private void returnGitkitWidget(String path, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html");

        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = request.getReader().readLine()) != null) {
                builder.append(line);
            }
            String postBody = URLEncoder.encode(builder.toString(), "UTF-8");
            response.getWriter().print(new Scanner(new File(servletContext
                    .getRealPath(path)), "UTF-8")
                    .useDelimiter("\\A").next()
                    .replaceAll("JAVASCRIPT_ESCAPED_POST_BODY", postBody));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

