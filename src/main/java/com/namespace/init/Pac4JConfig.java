package com.namespace.init;

import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.client.rest.CasRestBasicAuthClient;
import org.pac4j.cas.credentials.authenticator.CasRestAuthenticator;
import org.pac4j.core.authorization.Authorizer;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.http.client.indirect.IndirectBasicAuthClient;
import org.pac4j.http.credentials.authenticator.UsernamePasswordAuthenticator;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oidc.client.OidcClient;
import org.springframework.context.annotation.Bean;

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
    private static final String TWITTER_KEY = "CoxUiYwQOSFDReZYdjigBA";
    private static final String TWITTER_SECRET = "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs";
    private static final String JWT_SIGNING_SECRET = "12345678901234567890123456789012";
    private static final String JWT_ENCRYPTION_SECRET = "12345678901234567890123456789012";

    private static final short PORT_NUMBER = 8080;

    @Bean
    public Config config() {
        Map<String, Authorizer> authorizers = new HashMap<>();
        authorizers.put("admin", new RequireAnyRoleAuthorizer<>("ROLE_ADMIN"));

        OidcClient oidcClient = new OidcClient();
        oidcClient.setClientID(OID_CLIENT_ID);
        oidcClient.setSecret(OID_SECRET);
        oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
        oidcClient.setUseNonce(true);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("prompt", "consent");
        oidcClient.setCustomParams(paramMap);

        FacebookClient facebookClient = new FacebookClient();
        facebookClient.setKey(FACEBOOK_KEY);
        facebookClient.setSecret(FACEBOOK_SECRET);

        TwitterClient twitterClient = new TwitterClient();
        twitterClient.setKey(TWITTER_KEY);
        twitterClient.setSecret(TWITTER_SECRET);

        UsernamePasswordAuthenticator passwordAuthenticator = new SimpleTestUsernamePasswordAuthenticator();

        CasClient casClient = new CasClient();
        casClient.setCasLoginUrl("https://casserverpac4j.herokuapp.com/login");

        ParameterClient parameterClient = new ParameterClient("token",
                new JwtAuthenticator(JWT_SIGNING_SECRET, JWT_ENCRYPTION_SECRET));
        parameterClient.setSupportGetRequest(true);
        parameterClient.setSupportPostRequest(false);

        return new Config(new Clients("http://localhost:" + PORT_NUMBER + "/callback",
                oidcClient, facebookClient, twitterClient,
                new FormClient("http://localhost:"+ PORT_NUMBER + "/loginForm", passwordAuthenticator),
                new IndirectBasicAuthClient(passwordAuthenticator), parameterClient,
                new DirectBasicAuthClient(passwordAuthenticator),
                new CasRestBasicAuthClient(new CasRestAuthenticator("https://casserverpac4j.herokuapp.com/"),
                "Authorization", "Basic ")), authorizers);
    }
}
