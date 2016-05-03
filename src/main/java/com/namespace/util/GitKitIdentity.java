package com.namespace.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.namespace.model.Account;
import com.namespace.security.GitKitProfile;
import com.namespace.service.AccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;


/**
 * Created by Aaron on 03/05/2016.
 */
public class GitKitIdentity {

    @Autowired
    private static Environment environment;

    private GitKitIdentity() {
        throw new IllegalStateException("Cannot instantiate a utils class");
    }

    private static GoogleIdTokenVerifier VERIFIER = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
            .setAudience(Arrays.asList("78186330076-uh8feq9a83r0q0bs25t4q33o946se40e.apps.googleusercontent.com"))
            .setIssuer("https://accounts.google.com")
            .build();


    private static GitkitClient getGitkitClient() throws IOException, GitkitClientException {
        return  new GitkitClient.Builder()
                .setGoogleClientId(environment.getProperty("clientId"))
                .setProjectId(environment.getProperty("projectId"))
                .setServiceAccountEmail(environment.getProperty("serviceAccountEmail"))
                .setKeyStream(new FileInputStream(environment.getProperty("serviceAccountPrivateKeyFile")))
                .setWidgetUrl(environment.getProperty("widgetUrl"))
                .setCookieName(environment.getProperty("cookieName"))
                .build();
    }

    public static boolean userHasVerifiedEmail(HttpServletRequest request) {
        return userHasVerifiedEmail(getAuthTokenFromRequest(request));
    }

    public static boolean userHasVerifiedEmail(String authToken) {
        try {
            GoogleIdToken idToken = VERIFIER.verify(authToken);
            if (idToken != null) {
                return idToken.getPayload().getEmailVerified();
            }

            return false;
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getAuthTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if ("gtoken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    public static GitkitUser getUser(HttpServletRequest request) {
        try {
            GitkitClient gitkitClient = getGitkitClient();
            return gitkitClient.validateTokenInRequest(request);
        } catch (GitkitClientException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static GitkitUser getUser(String authToken) {
        try {
            GitkitClient gitkitClient = getGitkitClient();
            return gitkitClient.validateToken(authToken);
        } catch (GitkitClientException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Gitkit Widget
    public static void handleOauthCallback(ServletContext servletContext, HttpServletRequest request,
                                           HttpServletResponse response) {
        response.setContentType("text/html");

        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = request.getReader().readLine()) != null) {
                builder.append(line);
            }
            String postBody = URLEncoder.encode(builder.toString(), "UTF-8");
            response.getWriter().print(new Scanner(new File(servletContext
                    .getRealPath("static/gitkit-widget.html")), "UTF-8")
                    .useDelimiter("\\A").next()
                    .replaceAll("JAVASCRIPT_ESCAPED_POST_BODY", postBody));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    //Email Endpoint
    public static void sendEmail(HttpServletRequest request, HttpServletResponse resp) {
        try {
            GitkitClient gitkitClient = getGitkitClient();
            GitkitClient.OobResponse oobResponse = gitkitClient.getOobResponse(request);

            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress("aerisvulpe@gmail.com", "Spring Boilerplate Account"));
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(oobResponse.getEmail(), oobResponse.getRecipient()));
                if (oobResponse.getOobAction().equals(GitkitClient.OobAction.CHANGE_EMAIL)) {
                    msg.setSubject("Email address change for Spring Boilerplate account");
                    msg.setText("Hello!\n\n The email address for your Spring Boilerplate account will be changed from "
                            + oobResponse.getEmail() + " to " + oobResponse.getNewEmail() +
                            " when you click this confirmation link:\n\n " + oobResponse.getOobUrl().get() +
                            "\n\nIf you didn't request an email address change for this account, please disregard this message.");
                } else if (oobResponse.getOobAction().equals(GitkitClient.OobAction.RESET_PASSWORD)) {
                    msg.setSubject("Password change for Spring Boilerplate account");
                    msg.setText("Hello!\n\n The password for your Spring Boilerplate account will be reset " +
                            "when you click this confirmation link:\n\n " + oobResponse.getOobUrl().get() +
                            "\n\nIf you didn't request a password change for this account, please disregard this message.");
                }
                Transport.send(msg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            resp.getWriter().write(oobResponse.getResponseBody());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static GitKitProfile gitKitProfileFromUser(AccountManager accountManager, GitkitUser gitkitUser,
                                                      boolean isVerified) {
        boolean newAccount = false;
        Account account = accountManager.getAccountByNaturalId(gitkitUser.getLocalId());
        if (account == null) {
            account = new Account();
            newAccount = true;
        }
        String[] names = gitkitUser.getName().split(" ");
        String lastName = names.length > 1 ? names[names.length - 1] : "";
        account.setNaturalId(gitkitUser.getLocalId());
        account.setFirstName(names[0]);
        account.setLastName(lastName);
        account.setEmail(gitkitUser.getEmail());
        account.addRole(Account.ROLE_USER);
        account.addPermission(Account.PERMISSION_ENABLED);
        if (isVerified) {
            account.addPermission(Account.PERMISSION_EMAIL_VERTIFIED);
        }

        try {
            if (newAccount) {
                accountManager.createNewAccount(account);
            } else {
                accountManager.updateAccount(account);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return gitKitProfileFromAccount(account);
    }

    private static GitKitProfile gitKitProfileFromAccount(Account account) {
        final GitKitProfile profile = new GitKitProfile();
        profile.setId(account.getNaturalId());
        profile.addAttribute("account_id", account.getId());
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
        return profile;
    }
}
