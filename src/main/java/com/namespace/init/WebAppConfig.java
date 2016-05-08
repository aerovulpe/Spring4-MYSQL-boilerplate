package com.namespace.init;


import com.namespace.web.RequiresAuthenticationInterceptor;
import com.namespace.web.exception.*;
import com.sendgrid.SendGrid;
import cz.jirutka.spring.exhandler.RestHandlerExceptionResolver;
import org.pac4j.core.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;
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
public class WebAppConfig extends WebMvcConfigurerAdapter {

    private static final String PROPERTY_NAME_DATABASE_DRIVER = "db.driver";
    private static final String PROPERTY_NAME_HIBERNATE_DIALECT = "hibernate.dialect";
    private static final String PROPERTY_NAME_HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    private static final String PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN = "entitymanager.packages.to.scan";
    private static final String PROPERTY_NAME_JADIRA_USERTYPE_AUTO_REGISTER_USER_TYPES = "spring.jpa.properties.jadira.usertype.autoRegisterUserTypes";
    private static final String PROPERTY_NAME_SENDGRID_API_KEY = "sendgrid_api_key";


    @Resource
    private Environment environment;
    @Autowired
    private Config pac4JConfig;
    @Value("${pac4j.applicationLogout.defaultUrl:}")
    private String defaultUrl;
    @Value("${pac4j.applicationLogout.logoutUrlPattern:}")
    private String logoutUrlPattern;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(RestHandlerExceptionResolver.builder()
                .defaultContentType(MediaType.APPLICATION_JSON)
                .messageSource(messageSource())
                .addErrorMessageHandler(EmptyResultDataAccessException.class, HttpStatus.NOT_FOUND)
                .addErrorMessageHandler(BadRequestException.class, HttpStatus.BAD_REQUEST)
                .addErrorMessageHandler(ForbiddenException.class, HttpStatus.FORBIDDEN)
                .addErrorMessageHandler(InternalServerErrorException.class, HttpStatus.INTERNAL_SERVER_ERROR)
                .addErrorMessageHandler(NotFoundException.class, HttpStatus.NOT_FOUND)
                .addErrorMessageHandler(UnAuthorizedException.class, HttpStatus.UNAUTHORIZED)
                .build());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LocaleChangeInterceptor());
        registry.addInterceptor(new RequiresAuthenticationInterceptor(pac4JConfig, "HeaderTokenClient, GitkitClient", "user"))
                .addPathPatterns("/api/accounts/**");
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**").addResourceLocations("/assets/");
        registry.addResourceHandler("/static/**").addResourceLocations("/static/");
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(environment.getProperty(PROPERTY_NAME_SENDGRID_API_KEY));
    }

    @Bean
    public HibernateTransactionManager transactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
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
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_DRIVER));
        dataSource.setUrl(environment.getRequiredProperty("CLEARDB_DATABASE_URL"));
        dataSource.setUsername(environment.getRequiredProperty("CLEARDB_USERNAME"));
        dataSource.setPassword(environment.getRequiredProperty("CLEARDB_PASSWORD"));
        return dataSource;
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
