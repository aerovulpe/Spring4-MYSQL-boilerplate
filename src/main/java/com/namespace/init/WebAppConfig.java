package com.namespace.init;

import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.client.rest.CasRestBasicAuthClient;
import org.pac4j.cas.credentials.authenticator.CasRestAuthenticator;
import org.pac4j.core.authorization.Authorizer;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
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
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.*;

/**
 * Created by Aaron on 19/04/2016.
 */
@Configuration
@ComponentScan({"com.namespace", "org.pac4j.springframework.web"})
@EnableWebMvc
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class WebAppConfig extends WebMvcConfigurerAdapter {

    private static final String PROPERTY_NAME_DATABASE_DRIVER = "db.driver";
    private static final String PROPERTY_NAME_DATABASE_PASSWORD = "db.password";
    private static final String PROPERTY_NAME_DATABASE_URL = "db.url";
    private static final String PROPERTY_NAME_DATABASE_USERNAME = "db.username";
    private static final String PROPERTY_NAME_HIBERNATE_DIALECT = "hibernate.dialect";
    private static final String PROPERTY_NAME_HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    private static final String PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN = "entitymanager.packages.to.scan";

    private static final String OID_CLIENT_ID = "343992089165-sp0l1km383i8cbm2j5nn20kbk5dk8hor.apps.googleusercontent.com";
    private static final String OID_SECRET = "uR3D8ej1kIRPbqAFaxIE3HWh";
    private static final String FACEBOOK_KEY = "145278422258960";
    private static final String FACEBOOK_SECRET = "be21409ba8f39b5dae2a7de525484da8";
    private static final String TWITTER_KEY = "CoxUiYwQOSFDReZYdjigBA";
    private static final String TWITTER_SECRET = "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs";
    private static final String JWT_SIGNING_SECRET = "12345678901234567890123456789012";
    private static final String JWT_ENCRYPTION_SECRET = "12345678901234567890123456789012";

    private static final short PORT_NUMBER = 8080;

    @Resource
    private Environment environment;
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
    @Autowired
    private Authorizer adminRoleAuthorizer;
    @Autowired
    private Clients clients;
    @Autowired
    private Config config;

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LocaleChangeInterceptor());
        registry.addInterceptor(new RequiresAuthenticationInterceptor(config, "oidClient"))
                .addPathPatterns("login/oid");
        registry.addInterceptor(new RequiresAuthenticationInterceptor(config, "facebookClient"))
                .addPathPatterns("login/facebook");
        registry.addInterceptor(new RequiresAuthenticationInterceptor(config, "twitterClient"))
                .addPathPatterns("login/twitter");
        registry.addInterceptor(new RequiresAuthenticationInterceptor(config, "formClient"))
                .addPathPatterns("login/form");
        registry.addInterceptor(new RequiresAuthenticationInterceptor(config, "indirectBasicAuthClient"))
                .addPathPatterns("login/iba");
        registry.addInterceptor(new RequiresAuthenticationInterceptor(config, "casClient"))
                .addPathPatterns("login/cas");
        registry.addInterceptor(new RequiresAuthenticationInterceptor(config, "parameterClient"))
                .addPathPatterns("login/par");
        registry.addInterceptor(new RequiresAuthenticationInterceptor(config, "directBasicAuthClient"))
                .addPathPatterns("login/dba");
        registry.addInterceptor(new RequiresAuthenticationInterceptor(config, "casRestBasicClient"))
                .addPathPatterns("login/crb");
    }

    @Bean
    public InternalResourceViewResolver internalResourceViewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    @Bean
    public UrlBasedViewResolver viewResolver() {
        UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/views");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

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

    @Bean
    public RequireAnyRoleAuthorizer adminRoleAuthorizer() {
        return new RequireAnyRoleAuthorizer<>("ROLE_ADMIN");
    }

    @Bean
    public Config config() {
        Map<String, Authorizer> authorizers = new HashMap<>();
        authorizers.put("admin", adminRoleAuthorizer);
        return new Config(clients, authorizers);
    }

    @Bean
    public UrlBasedViewResolver tilesViewResolver() {
        UrlBasedViewResolver resolver = new UrlBasedViewResolver();
        resolver.setViewClass(TilesView.class);
        return resolver;
    }

    @Bean
    public TilesConfigurer tilesConfigurer(){
        TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions("/WEB-INF/views/**/views.xml");
        return tilesConfigurer;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_DRIVER));
        dataSource.setUrl(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_URL));
        dataSource.setUsername(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_USERNAME));
        dataSource.setPassword(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_PASSWORD));
        return dataSource;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource());
        sessionFactoryBean.setPackagesToScan(environment.getRequiredProperty(PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN));
        sessionFactoryBean.setHibernateProperties(hibProperties());
        return sessionFactoryBean;
    }

    @Bean
    public HibernateTransactionManager transactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }

    private Properties hibProperties() {
        Properties properties = new Properties();
        properties.put(PROPERTY_NAME_HIBERNATE_DIALECT, environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_DIALECT));
        properties.put(PROPERTY_NAME_HIBERNATE_SHOW_SQL, environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_SHOW_SQL));
        return properties;
    }
}
