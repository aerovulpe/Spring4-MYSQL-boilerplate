package com.namespace.controller;

import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitUser;
import com.namespace.init.Pac4JConfig;
import com.namespace.model.Account;
import com.namespace.security.HttpProfile;
import com.namespace.security.TimedJwtGenerator;
import com.namespace.service.AccountManager;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Handles requests for the application home page.
 */
@Controller
public class LoginController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    ServletContext servletContext;
    @Autowired
    AccountManager accountManager;

    public LoginController() {
    }

    @RequestMapping(value = "/", method = GET)
    public String home(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) {
        CommonProfile profile = getProfile(request, response);
        if (profile != null)
            logger.info("Welcome home, " + profile.getFirstName() + "!");
        map.put("profile", profile);
        return "/home/home";
    }

    @RequestMapping(value = "/loginfailed", method = GET)
    public String loginError(ModelMap model) {
        model.addAttribute("error", "true");
        return "/signin/signin";
    }

    @RequestMapping(value = "/login", method = GET)
    public String getLoginPage() {
        return "/signin/signin";
    }

    @RequestMapping("/login/facebook")
    public String facebook(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) {
        map.put("profile", getProfile(request, response));
        return "redirect:/";
    }

    @RequestMapping("/login/iba")
    public String form(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) {
        map.put("profile", getProfile(request, response));
        return "redirect:/";
    }

    @RequestMapping("/login/oidc")
    public String oidc(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) {
        map.put("profile", getProfile(request, response));
        return "redirect:/";
    }

    @RequestMapping(value = "/jwt.html", method = GET)
    public String jwt(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) {
        final CommonProfile profile = getProfile(request, response);
        final TimedJwtGenerator<CommonProfile> generator = new TimedJwtGenerator<>(Pac4JConfig.JWT_SIGNING_SECRET,
                Pac4JConfig.JWT_ENCRYPTION_SECRET);
        String token = "";
        if (profile != null) {
            token = generator.generate(profile);
        }
        map.put("token", token);
        return "/jwt";
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

            ProfileManager<HttpProfile> httpProfileProfileManager =
                    new ProfileManager<>(new J2EContext(request, response));
            Account account = accountManager.getAccountByUsername(gitkitUser.getLocalId());
            if (account == null) {
                logger.info("New account");
                String[] names = gitkitUser.getName().split(" ");
                account = new Account(gitkitUser.getLocalId(), null, names[0], names[1], gitkitUser.getEmail(),
                        new HashSet<>(), new HashSet<>());
                account.addRole(Account.ROLE_USER);
                account.addPermission(Account.PERMISSION_ENABLED);
                account.addPermission(Account.PERMISSION_EMAIL_VERTIFIED);
                accountManager.createNewAccount(account);
            }

            logger.info("Account: " + account.toString());
            final HttpProfile profile = new HttpProfile();
            profile.setId(account.getId());
            profile.addAttribute("account_id", account.getId());
            profile.addAttribute(Pac4jConstants.USERNAME, account.getUsername());
            profile.addAttribute("email", account.getEmail());
            profile.addAttribute("first_name", account.getFirstName());
            profile.addAttribute("family_name", account.getLastName());
            profile.addAttribute("name", account.getFirstName() + " " + account.getLastName());
            profile.addAttribute("display_name", account.getFirstName());
            profile.addAttribute("gender", account.getGender());
            profile.addAttribute("locale", account.getLocale());
            profile.addAttribute("picture_url", account.getPictureUrl());
            profile.addAttribute("location", account.getLocation());
            profile.addAttribute("ip", request.getRemoteAddr());
            profile.setRemembered(account.isRemembered());
            profile.addRoles(new ArrayList<>(account.getRoles()));
            profile.addPermissions(new ArrayList<>(account.getPermissions()));

            httpProfileProfileManager.save(true, profile, false);
        }

        return "redirect:/";
    }

    @RequestMapping("/oauth2callback")
    public void gitkitWidget(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");

        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = request.getReader().readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String postBody = URLEncoder.encode(builder.toString(), "UTF-8");

        try {
            response.getWriter().print(new Scanner(new File(servletContext
                    .getRealPath("/WEB-INF/static/gitkit-widget.html")), "UTF-8")
                    .useDelimiter("\\A").next()
                    .replaceAll("JAVASCRIPT_ESCAPED_POST_BODY", postBody));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().print(e.toString());
        }
    }
}

