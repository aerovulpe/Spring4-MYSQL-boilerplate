package com.namespace.security;

import com.namespace.model.Account;
import com.namespace.service.AccountManager;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.UsernamePasswordAuthenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.profile.IpProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

/**
 * Created by Aaron on 20/04/2016.
 */
@Component
public class BCryptUsernamePasswordAuthenticator implements UsernamePasswordAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(BCryptUsernamePasswordAuthenticator.class);

    @Autowired
    private AccountManager accountManager;
    @Autowired
    private HttpServletRequest request;

    public BCryptUsernamePasswordAuthenticator() {
    }

    @Override
    public void validate(UsernamePasswordCredentials credentials) {
        if (credentials == null) {
            throwsException("No credential");
        }
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        logger.info("Username: " + username + ". Password:" + password);
        if (CommonHelper.isBlank(username)) {
            throwsException("Username cannot be blank");
        }
        if (CommonHelper.isBlank(password)) {
            throwsException("Password cannot be blank");
        }

        Account account = accountManager.getAccountByUsername(username);
        if (account == null) {
            throwsException("Account not found");
        }

        logger.info("Account: " + account.toString());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throwsException("Username : '" + username + "'s password does not match password in database");
        }

        final IpProfile profile = new IpProfile();
        String ipAddress = request.getRemoteAddr();

        try {
            accountManager.seenIpAddress(account, ipAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }

        profile.setId(ipAddress);
        profile.addAttribute(Pac4jConstants.USERNAME, username);
        profile.addAttribute("email", account.getEmail());
        profile.addAttribute("first_name", account.getFirstName());
        profile.addAttribute("family_name", account.getLastName());
        profile.addAttribute("name", account.getFirstName() + " " + account.getLastName());
        profile.addAttribute("display_name", account.getFirstName());
        profile.addAttribute("gender", account.getGender());
        profile.addAttribute("locale", account.getLocale());
        profile.addAttribute("picture_url", account.getPictureUrl());
        profile.addAttribute("location", account.getLocation());
        profile.setRemembered(account.isRemembered());
        profile.addRoles(new ArrayList<>(account.getRoles()));
        profile.addPermissions(new ArrayList<>(account.getPermissions()));

        credentials.setUserProfile(profile);
        accountManager.updateAccount(account);
    }

    private void throwsException(final String message) {
        throw new CredentialsException(message);
    }
}
