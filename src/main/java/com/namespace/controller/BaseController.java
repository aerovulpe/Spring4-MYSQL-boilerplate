package com.namespace.controller;

import com.namespace.model.Account;
import com.namespace.security.GitKitProfile;
import com.namespace.service.AccountManager;
import com.namespace.util.GitKitIdentity;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
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
    protected AccountManager accountManager;

    protected CommonProfile getProfile(HttpServletRequest request, HttpServletResponse response) {
        final WebContext context = new J2EContext(request, response);
        final ProfileManager<CommonProfile> manager = new ProfileManager<>(context);
        Optional<CommonProfile> profile = manager.get(true);

        if (profile.isPresent()) {
            return profile.get();
        }

        // Fallback
        GitKitProfile gitKitProfile = GitKitIdentity.getGitKitProfile(accountManager, request, false);
        if (gitKitProfile != null) {
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
        account.setGender(profile.getGender());
        account.setLocale(profile.getLocale() == null ? null : profile.getLocale().toLanguageTag());
        account.setLocation(profile.getLocation());
        account.setRemembered(profile.isRemembered());
        account.setRoles(new HashSet<>(profile.getRoles()));
        account.setPermissions(new HashSet<>(profile.getPermissions()));
        return account;
    }

    private static String getUserNaturalId(CommonProfile profile) {
        return profile.getId();
    }

    void serveHtmlPage(String path, HttpServletResponse response) {
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
