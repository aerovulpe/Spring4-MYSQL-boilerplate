package com.namespace.web.controller;

import com.namespace.init.Pac4JConfig;
import com.namespace.security.TimedJwtGenerator;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    public HomeController() {
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
}

