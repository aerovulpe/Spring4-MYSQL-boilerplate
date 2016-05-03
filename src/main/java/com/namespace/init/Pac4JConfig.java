package com.namespace.init;

import com.namespace.model.Account;
import com.namespace.security.BCryptUsernamePasswordAuthenticator;
import com.namespace.security.HeaderTokenClient;
import com.namespace.security.RolesPermissionsAuthorizationGenerator;
import com.namespace.security.TimedJwtAuthenticator;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.authorizer.RequireAllRolesAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.indirect.IndirectBasicAuthClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oidc.client.GoogleOidcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aaron on 20/04/2016.
 */
public class Pac4JConfig {
    private static final String OID_CLIENT_ID = "343992089165-sp0l1km383i8cbm2j5nn20kbk5dk8hor.apps.googleusercontent.com";
    private static final String OID_SECRET = "uR3D8ej1kIRPbqAFaxIE3HWh";
    private static final String FACEBOOK_KEY = "145278422258960";
    private static final String FACEBOOK_SECRET = "be21409ba8f39b5dae2a7de525484da8";
    public static final String JWT_SIGNING_SECRET = "12345678901234567890123456789012";
    public static final String JWT_ENCRYPTION_SECRET = "12345678901234567890123456789012";

    @Autowired
    BCryptUsernamePasswordAuthenticator passwordAuthenticator;
    @Autowired
    RolesPermissionsAuthorizationGenerator rolesPermissionsAuthorizationGenerator;
    @Autowired
    Environment environment;

    @Bean
    @SuppressWarnings("unchecked")
    public Config pac4JConfig() {
        Map<String, Authorizer> authorizers = new HashMap<>();
        authorizers.put("admin", new RequireAllRolesAuthorizer<>(Account.ROLE_ADMIN, Account.ROLE_USER));
        authorizers.put("user", new RequireAllRolesAuthorizer<>(Account.ROLE_USER));

        rolesPermissionsAuthorizationGenerator.setDefaultRoles(Account.ROLE_USER);
        rolesPermissionsAuthorizationGenerator.setDefaultPermissions(Account.PERMISSION_ENABLED,
                Account.PERMISSION_EMAIL_VERTIFIED);

        GoogleOidcClient googleOidcClient = new GoogleOidcClient();
        googleOidcClient.setClientID(OID_CLIENT_ID);
        googleOidcClient.setSecret(OID_SECRET);
        googleOidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
        googleOidcClient.setUseNonce(true);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("prompt", "consent");
        googleOidcClient.setCustomParams(paramMap);
        googleOidcClient.setAuthorizationGenerator(rolesPermissionsAuthorizationGenerator);

        FacebookClient facebookClient = new FacebookClient();
        facebookClient.setKey(FACEBOOK_KEY);
        facebookClient.setSecret(FACEBOOK_SECRET);
        facebookClient.setAuthorizationGenerator(rolesPermissionsAuthorizationGenerator);

        HeaderTokenClient headerTokenClient = new HeaderTokenClient("access_token",
                new TimedJwtAuthenticator(JWT_SIGNING_SECRET, JWT_ENCRYPTION_SECRET));

        return new Config(new Clients("http://localhost:8080/callback",
                googleOidcClient, facebookClient, new IndirectBasicAuthClient(passwordAuthenticator),
                new DirectBasicAuthClient(passwordAuthenticator),
                headerTokenClient), authorizers);
    }
}
