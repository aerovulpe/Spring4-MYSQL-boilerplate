package com.namespace.controller;

import com.google.identitytoolkit.GitkitUser;
import com.namespace.security.GitKitProfile;
import com.namespace.service.AccountManager;
import com.namespace.util.GitKitIdentity;
import com.namespace.util.Utils;
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

        GitkitUser gitkitUser = GitKitIdentity.getUser(request);
        if (gitkitUser != null) {
            GitKitProfile gitKitProfile = GitKitIdentity.gitKitProfileFromUser(accountManager, gitkitUser,
                    GitKitIdentity.userHasVerifiedEmail(request), false);
            if (gitKitProfile != null) {
                manager.save(true, gitKitProfile, false);
            }
            return gitKitProfile;
        }

        return null;
    }

    protected String getUserNaturalId(HttpServletRequest request, HttpServletResponse response) {
        return Utils.getUserNaturalId(getProfile(request, response));
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
