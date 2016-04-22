package com.namespace.controller;

import com.namespace.util.Utils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Aaron on 20/03/2016.
 */
public abstract class BaseController {

    protected CommonProfile getProfile(HttpServletRequest request, HttpServletResponse response) {
        final WebContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        return (CommonProfile) manager.get(true);
    }

    protected String getUserName(HttpServletRequest request, HttpServletResponse response){
        return Utils.getUserName(getProfile(request, response));
    }
}
