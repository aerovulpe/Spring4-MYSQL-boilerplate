package com.namespace.web;


import org.pac4j.core.authorization.checker.AuthorizationChecker;
import org.pac4j.core.authorization.checker.DefaultAuthorizationChecker;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.client.finder.ClientFinder;
import org.pac4j.core.client.finder.DefaultClientFinder;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RequiresAuthenticationInterceptor extends HandlerInterceptorAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ClientFinder clientFinder = new DefaultClientFinder();

    protected AuthorizationChecker authorizationChecker = new DefaultAuthorizationChecker();

    protected Config config;

    protected String clientName;

    protected String authorizerName;

    public RequiresAuthenticationInterceptor(final Config config) {
        this.config = config;
    }

    public RequiresAuthenticationInterceptor(final Config config, final String clientName) {
        this(config);
        this.clientName = clientName;
    }

    public RequiresAuthenticationInterceptor(final Config config, final String clientName, final String authorizerName) {
        this(config, clientName);
        this.authorizerName = authorizerName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        final WebContext context = new J2EContext(request, response);
        logger.debug("url: {}", context.getFullRequestURL());

        CommonHelper.assertNotNull("config", config);
        final Clients configClients = config.getClients();
        CommonHelper.assertNotNull("configClients", configClients);
        logger.debug("clientName: {}", clientName);
        final List<Client> currentClients = clientFinder.find(configClients, context, this.clientName);
        logger.debug("currentClients: {}", currentClients);

        final boolean useSession = useSession(context, currentClients);
        logger.debug("useSession: {}", useSession);
        final ProfileManager<CommonProfile> manager = new ProfileManager<>(context);
        Optional<CommonProfile> optionalProfile = manager.get(useSession);
        CommonProfile profile = optionalProfile.isPresent() ? optionalProfile.get() : null;
        logger.debug("profile: {}", profile);

        // no profile and some current clients
        if (profile == null && currentClients != null && currentClients.size() > 0) {
            // loop on all clients searching direct ones to perform authentication
            for (final Client currentClient : currentClients) {
                if (currentClient instanceof DirectClient) {
                    logger.debug("Performing authentication for client: {}", currentClient);
                    final Credentials credentials;
                    try {
                        credentials = currentClient.getCredentials(context);
                        logger.debug("credentials: {}", credentials);
                    } catch (final RequiresHttpAction e) {
                        logger.debug("extra HTTP action required: {}", e.getCode());
                        return false;
                    }
                    profile = currentClient.getUserProfile(credentials, context);
                    logger.debug("profile: {}", profile);
                    if (profile != null) {
                        manager.save(useSession, profile, false);
                        break;
                    }
                }
            }
        }

        if (profile != null) {
            logger.debug("authorizerName: {}", authorizerName);
            if (authorizationChecker.isAuthorized(context, Collections.singletonList(profile),
                    authorizerName, config.getAuthorizers())) {
                logger.debug("grant access");

                return true;
            } else {
                logger.debug("forbidden");
                forbidden(context, currentClients, profile);
            }
        } else {
            if (startAuthentication(context, currentClients)) {
                logger.debug("Starting authentication");
                saveRequestedUrl(context, currentClients);
                redirectToIdentityProvider(context, currentClients);
            } else {
                logger.debug("unauthorized");
                unauthorized(context, currentClients);

            }
        }

        return false;
    }

    protected boolean useSession(final WebContext context, final List<Client> currentClients) {
        return currentClients == null || currentClients.size() == 0 || currentClients.get(0) instanceof IndirectClient;
    }

    protected void forbidden(final WebContext context, final List<Client> currentClients, final UserProfile profile) {
        context.setResponseStatus(HttpConstants.FORBIDDEN);
    }

    protected boolean startAuthentication(final WebContext context, final List<Client> currentClients) {
        return currentClients != null && currentClients.size() > 0 && currentClients.get(0) instanceof IndirectClient;
    }

    protected void saveRequestedUrl(final WebContext context, final List<Client> currentClients) {
        final String requestedUrl = context.getFullRequestURL();
        logger.debug("requestedUrl: {}", requestedUrl);
        context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
    }

    protected void redirectToIdentityProvider(final WebContext context, final List<Client> currentClients) {
        try {
            final IndirectClient currentClient = (IndirectClient) currentClients.get(0);
            currentClient.redirect(context);
        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required: {}", e.getCode());
        }
    }

    protected void unauthorized(final WebContext context, final List<Client> currentClients) {
        context.setResponseStatus(HttpConstants.UNAUTHORIZED);
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getAuthorizerName() {
        return authorizerName;
    }

    public void setAuthorizerName(String authorizerName) {
        this.authorizerName = authorizerName;
    }
}
