package com.namespace.controller;

import com.namespace.model.Account;
import com.namespace.security.gitkit.GitKitProfile;
import com.namespace.service.GitKitIdentityService;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Scanner;

/**
 * Created by Aaron on 20/03/2016.
 */
public abstract class BaseController {

    @Autowired
    protected ServletContext servletContext;
    @Autowired
    private GitKitIdentityService gitKitIdentityService;

    protected CommonProfile getProfile(HttpServletRequest request, HttpServletResponse response) {
        final WebContext context = new J2EContext(request, response);
        final ProfileManager<CommonProfile> manager = new ProfileManager<>(context);
        Optional<CommonProfile> profile = manager.get(true);

        if (profile.isPresent()) {
            return profile.get();
        }

        // Fallback
        GitKitProfile gitKitProfile = gitKitIdentityService.getGitKitProfile(request, true);
        if (gitKitProfile != null) {
            LoggerFactory.getLogger(BaseController.class).info("Fallback!");
            manager.save(true, gitKitProfile, false);
        }

        return gitKitProfile;
    }

    protected Account accountFromProfile(CommonProfile profile) {
        Account account = new Account();
        if (profile.getAttribute("account_id") != null)
            account.setId(profile.getAttribute("account_id", Long.class));
        account.setNaturalId(getUserNaturalId(profile));
        account.setFirstName(profile.getFirstName());
        account.setLastName(profile.getFamilyName());
        account.setEmail(profile.getEmail());
        account.setPictureUrl(profile.getPictureUrl());
        account.setRoles(new HashSet<>(profile.getRoles()));
        account.setPermissions(new HashSet<>(profile.getPermissions()));
        return account;
    }

    private String getUserNaturalId(CommonProfile profile) {
        return profile.getId();
    }

    protected String getUserNaturalId(HttpServletRequest request) {
        return gitKitIdentityService.getUserLocalId(request);
    }

    protected void serveHtmlPage(String path, HttpServletResponse response) {
        response.setContentType("text/html");
        try {
            response.getWriter().print(new Scanner(new File(servletContext
                    .getRealPath(path)), "UTF-8")
                    .useDelimiter("\\A").next());
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
