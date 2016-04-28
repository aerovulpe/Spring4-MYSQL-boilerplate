package com.namespace.controller;

import com.namespace.util.Utils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Created by Aaron on 20/03/2016.
 */
public abstract class BaseController {

    @SuppressWarnings("unchecked")
    protected CommonProfile getProfile(HttpServletRequest request, HttpServletResponse response) {
        final WebContext context = new J2EContext(request, response);
        final ProfileManager<CommonProfile> manager = new ProfileManager<>(context);
        Optional<CommonProfile> profile = manager.get(true);
        return profile.isPresent() ? profile.get() : null;
    }

    protected String getUserNaturalId(HttpServletRequest request, HttpServletResponse response){
        return Utils.getUserNaturalId(getProfile(request, response));
    }
}
