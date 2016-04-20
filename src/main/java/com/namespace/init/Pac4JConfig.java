package com.namespace.init;

import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.client.rest.CasRestBasicAuthClient;
import org.pac4j.cas.credentials.authenticator.CasRestAuthenticator;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.http.client.indirect.IndirectBasicAuthClient;
import org.pac4j.http.credentials.authenticator.Authenticator;
import org.pac4j.http.credentials.authenticator.UsernamePasswordAuthenticator;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oidc.client.OidcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private UsernamePasswordAuthenticator passwordAuthenticator;
    @Autowired
    private Authenticator casAuthenticator;
    @Autowired
    private Client oidClient;
    @Autowired
    private FacebookClient facebookClient;
    @Autowired
    private TwitterClient twitterClient;
    @Autowired
    private FormClient formClient;
    @Autowired
    private IndirectBasicAuthClient indirectBasicAuthClient;
    @Autowired
    private CasClient casClient;
    @Autowired
    private ParameterClient parameterClient;
    @Autowired
    private DirectBasicAuthClient directBasicAuthClient;
    @Autowired
    private CasRestBasicAuthClient casRestBasicAuthClient;

    @Bean
    public OidcClient oidClient() {
        OidcClient oidcClient = new OidcClient();
        oidcClient.setClientID(OID_CLIENT_ID);
        oidcClient.setSecret(OID_SECRET);
        oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
        oidcClient.setUseNonce(true);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("prompt", "consent");
        oidcClient.setCustomParams(paramMap);
        return oidcClient;
    }

    @Bean
    public FacebookClient facebookClient() {
        FacebookClient facebookClient = new FacebookClient();
        facebookClient.setKey(FACEBOOK_KEY);
        facebookClient.setSecret(FACEBOOK_SECRET);
        return facebookClient;
    }

    @Bean
    public TwitterClient twitterClient() {
        TwitterClient twitterClient = new TwitterClient();
        twitterClient.setKey(TWITTER_KEY);
        twitterClient.setSecret(TWITTER_SECRET);
        return twitterClient;
    }

    @Bean
    public SimpleTestUsernamePasswordAuthenticator passwordAuthenticator() {
        return new SimpleTestUsernamePasswordAuthenticator();
    }

    @Bean
    public FormClient formClient() {
        return new FormClient("http://localhost:8080/loginForm", passwordAuthenticator);
    }

    @Bean
    public IndirectBasicAuthClient indirectBasicAuthClient() {
        return new IndirectBasicAuthClient(passwordAuthenticator);
    }

    @Bean
    public CasRestAuthenticator casAuthenticator() {
        return new CasRestAuthenticator("https://casserverpac4j.herokuapp.com/");
    }

    @Bean
    public CasRestBasicAuthClient casRestBasicAuthClient() {
        return new CasRestBasicAuthClient(casAuthenticator, "Authorization", "Basic ");
    }

    @Bean
    public CasClient casClient() {
        CasClient casClient = new CasClient();
        casClient.setCasLoginUrl("https://casserverpac4j.herokuapp.com/login");
        return casClient;
    }

    @Bean
    public ParameterClient parameterClient() {
        ParameterClient parameterClient = new ParameterClient("token",
                new JwtAuthenticator(JWT_SIGNING_SECRET, JWT_ENCRYPTION_SECRET));
        parameterClient.setSupportGetRequest(true);
        parameterClient.setSupportPostRequest(false);
        return parameterClient;
    }

    @Bean
    public DirectBasicAuthClient directBasicAuthClient() {
        return new DirectBasicAuthClient(passwordAuthenticator);
    }

    @Bean
    public Clients clients() {
        List<Client> clients = new ArrayList<>();
        clients.add(oidClient);
        clients.add(facebookClient);
        clients.add(twitterClient);
        clients.add(formClient);
        clients.add(indirectBasicAuthClient);
        clients.add(casClient);
        clients.add(parameterClient);
        clients.add(directBasicAuthClient);
        clients.add(casRestBasicAuthClient);
        return new Clients("http://localhost:" + PORT_NUMBER + "/callback", clients);
    }
}
