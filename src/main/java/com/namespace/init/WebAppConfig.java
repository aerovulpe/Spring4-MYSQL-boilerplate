package com.namespace.init;


import com.namespace.web.RequiresAuthenticationInterceptor;
import org.pac4j.core.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by Aaron on 19/04/2016.
 */
@Configuration
@ComponentScan({"com.namespace"})
@Import({Pac4JConfig.class})
@EnableWebMvc
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
@PropertySource("classpath:email.properties")
public class WebAppConfig extends WebMvcConfigurerAdapter {

    private static final String PROPERTY_NAME_DATABASE_DRIVER = "db.driver";
    private static final String PROPERTY_NAME_DATABASE_PASSWORD = "db.password";
    private static final String PROPERTY_NAME_DATABASE_URL = "db.url";
    private static final String PROPERTY_NAME_DATABASE_USERNAME = "db.username";
    private static final String PROPERTY_NAME_HIBERNATE_DIALECT = "hibernate.dialect";
    private static final String PROPERTY_NAME_HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    private static final String PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN = "entitymanager.packages.to.scan";
    private static final String PROPERTY_NAME_JADIRA_USERTYPE_AUTO_REGISTER_USER_TYPES = "spring.jpa.properties.jadira.usertype.autoRegisterUserTypes";
    private static final String PROPERTY_NAME_EMAIL_HOST = "email.host";
    private static final String PROPERTY_NAME_EMAIL_PORT = "email.port";
    private static final String PROPERTY_NAME_EMAIL_USERNAME = "email.username";
    private static final String PROPERTY_NAME_EMAIL_PASSWORD = "email.password";
    private static final String PROPERTY_NAME_EMAIL_TRANSPORT_PROTOCOL = "email.transport.protocol";
    private static final String PROPERTY_NAME_EMAIL_FROM_EMAIL = "email.from.email";


    @Resource
    private Environment environment;
    @Autowired
    private Config pac4JConfig;
    @Value("${pac4j.applicationLogout.defaultUrl:}")
    private String defaultUrl;
    @Value("${pac4j.applicationLogout.logoutUrlPattern:}")
    private String logoutUrlPattern;

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**").addResourceLocations("/assets/");
        registry.addResourceHandler("/static/**").addResourceLocations("/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LocaleChangeInterceptor());
        registry.addInterceptor(new RequiresAuthenticationInterceptor(pac4JConfig, "HeaderTokenClient", "user"))
                .addPathPatterns("/api/accounts/**");
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(environment.getProperty(PROPERTY_NAME_EMAIL_HOST));
        mailSender.setPort(Integer.parseInt(environment.getProperty(PROPERTY_NAME_EMAIL_PORT)));
        mailSender.setUsername(environment.getProperty(PROPERTY_NAME_EMAIL_USERNAME));
        mailSender.setPassword(environment.getProperty(PROPERTY_NAME_EMAIL_PASSWORD));
        mailSender.setProtocol(environment.getProperty(PROPERTY_NAME_EMAIL_TRANSPORT_PROTOCOL));

        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.auth", true);
        javaMailProperties.put("mail.smtp.starttls.enable", true);
        javaMailProperties.put("mail.from.email", environment.getRequiredProperty(PROPERTY_NAME_EMAIL_FROM_EMAIL));

        mailSender.setJavaMailProperties(javaMailProperties);
        return mailSender;
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
        sessionFactoryBean.setPackagesToScan(environment
                .getRequiredProperty(PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN));
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
        properties.put(PROPERTY_NAME_HIBERNATE_DIALECT, environment
                .getRequiredProperty(PROPERTY_NAME_HIBERNATE_DIALECT));
        properties.put(PROPERTY_NAME_HIBERNATE_SHOW_SQL, environment
                .getRequiredProperty(PROPERTY_NAME_HIBERNATE_SHOW_SQL));
        properties.put(PROPERTY_NAME_JADIRA_USERTYPE_AUTO_REGISTER_USER_TYPES,
                environment.getRequiredProperty(PROPERTY_NAME_JADIRA_USERTYPE_AUTO_REGISTER_USER_TYPES));
        return properties;
    }
}
