package com.namespace.init;

import com.namespace.model.Account;
import com.namespace.security.GitkitAuthenticator;
import com.namespace.security.GitkitClient;
import com.namespace.security.HeaderTokenClient;
import com.namespace.security.TimedJwtAuthenticator;
import com.namespace.service.GitKitIdentityService;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.authorizer.RequireAllRolesAuthorizer;
import org.pac4j.core.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aaron on 20/04/2016.
 */
public class Pac4JConfig {
    public static final String JWT_SIGNING_SECRET = "12345678901234567890123456789012";
    public static final String JWT_ENCRYPTION_SECRET = "12345678901234567890123456789012";

    @Autowired
    private GitKitIdentityService gitKitIdentityService;

    @Bean
    @SuppressWarnings("unchecked")
    public Config pac4JConfig() {
        Map<String, Authorizer> authorizers = new HashMap<>();
        authorizers.put("admin", new RequireAllRolesAuthorizer<>(Account.ROLE_ADMIN, Account.ROLE_USER));
        authorizers.put("user", new RequireAllRolesAuthorizer<>(Account.ROLE_USER));

        return new Config(authorizers, new HeaderTokenClient("access_token",
                new TimedJwtAuthenticator(JWT_SIGNING_SECRET, JWT_ENCRYPTION_SECRET)),
                new GitkitClient("gtoken", new GitkitAuthenticator(gitKitIdentityService)));
    }
}
