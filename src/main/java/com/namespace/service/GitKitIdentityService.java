package com.namespace.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitServerException;
import com.google.identitytoolkit.GitkitUser;
import com.namespace.model.Account;
import com.namespace.security.GitKitProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


/**
 * Created by Aaron on 03/05/2016.
 */
@Service
public class GitKitIdentityService {
    @Autowired
    private MailSender mailSender;
    @Autowired
    private ServletContext servletContext;
    @Autowired
    private AccountManager accountManager;

    public GitKitIdentityService() {
    }

    private GoogleIdTokenVerifier VERIFIER = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
            .setAudience(Arrays.asList("78186330076-uh8feq9a83r0q0bs25t4q33o946se40e.apps.googleusercontent.com"))
            .setIssuer("https://accounts.google.com")
            .build();


    private GitkitClient getGitkitClient() throws IOException, GitkitClientException {
        return GitkitClient.createFromJson(getClass().getClassLoader()
                .getResource("gitkit-server-config.json").getPath().substring(1));
    }

    public boolean userHasVerifiedEmail(HttpServletRequest request) {
        return userHasVerifiedEmail(getAuthTokenFromRequest(request));
    }

    public boolean userHasVerifiedEmail(String authToken) {
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

    private String getAuthTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("gtoken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    public GitkitUser getUser(HttpServletRequest request) {
        try {
            GitkitClient gitkitClient = getGitkitClient();
            return gitkitClient.validateTokenInRequest(request);
        } catch (GitkitClientException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public GitkitUser getUser(String authToken) {
        try {
            GitkitClient gitkitClient = getGitkitClient();
            return gitkitClient.validateToken(authToken);
        } catch (GitkitClientException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Gitkit Widget
    public void handleOauthCallback(HttpServletRequest request,
                                    HttpServletResponse response) {
        response.setContentType("text/html");

        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = request.getReader().readLine()) != null) {
                builder.append(line);
            }
            String postBody = URLEncoder.encode(builder.toString(), "UTF-8");
            response.getWriter().print(new Scanner(new File(servletContext.getRealPath("/static/gitkit-widget.html")), "UTF-8")
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
    public void sendEmail(HttpServletRequest request, HttpServletResponse resp) {
        try {
            GitkitClient gitkitClient = getGitkitClient();
            GitkitClient.OobResponse oobResponse = gitkitClient.getOobResponse(request);

            try {
                String subject;
                String text;
                if (oobResponse.getOobAction().equals(GitkitClient.OobAction.CHANGE_EMAIL)) {
                    subject = "Email address change for Spring Boilerplate account";
                    text = "Hello!\n\n The email address for your Spring Boilerplate account will be changed from "
                            + oobResponse.getEmail() + " to " + oobResponse.getNewEmail() +
                            " when you click this confirmation link:\n\n " + oobResponse.getOobUrl().get() +
                            "\n\nIf you didn't request an email address change for this account, please disregard this message.";
                    sendEmail(oobResponse.getEmail(), subject, text);
                } else if (oobResponse.getOobAction().equals(GitkitClient.OobAction.RESET_PASSWORD)) {
                    subject = "Password change for Spring Boilerplate account";
                    text = "Hello!\n\n The password for your Spring Boilerplate account will be reset " +
                            "when you click this confirmation link:\n\n " + oobResponse.getOobUrl().get() +
                            "\n\nIf you didn't request a password change for this account, please disregard this message.";
                    sendEmail(oobResponse.getEmail(), subject, text);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            resp.getWriter().write(oobResponse.getResponseBody());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GitKitProfile getGitKitProfile(HttpServletRequest request,
                                          boolean updateAccount) {
        return getGitKitProfile(getAuthTokenFromRequest(request), updateAccount);
    }

    public GitKitProfile getGitKitProfile(String gtoken, boolean updateAccount) {
        GitkitUser gitkitUser = getUser(gtoken);
        if (gitkitUser == null) {
            return null;
        }

        Account account = accountManager.getAccountByNaturalId(gitkitUser.getLocalId());
        boolean newAccount = account == null;

        if (!newAccount && !updateAccount) {
            return gitKitProfileFromAccount(account);
        }

        if (newAccount) {
            account = new Account();
        }
        if (gitkitUser.getName() != null) {
            String[] names = gitkitUser.getName().split(" ");
            String lastName = names.length > 1 ? names[names.length - 1] : "";
            account.setFirstName(names[0]);
            account.setLastName(lastName);
        } else {
            account.setFirstName("");
            account.setLastName("");
        }
        account.setNaturalId(gitkitUser.getLocalId());
        account.setPictureUrl(gitkitUser.getPhotoUrl());
        account.setEmail(gitkitUser.getEmail());
        if (userHasVerifiedEmail(gtoken)) {
            account.addPermission(Account.PERMISSION_EMAIL_VERTIFIED);
        } else {
            try {
                sendVerificationEmail(gitkitUser.getEmail(),
                        getGitkitClient().getEmailVerificationLink(gitkitUser.getEmail()));
            } catch (GitkitServerException | GitkitClientException | IOException | MessagingException e) {
                e.printStackTrace();
            }
        }

        try {
            if (newAccount) {
                account.addRole(Account.ROLE_USER);
                account.addPermission(Account.PERMISSION_ENABLED);
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

    private void sendVerificationEmail(String recipientEmail, String emailVerificationLink)
            throws UnsupportedEncodingException, MessagingException {
        String subject = "Verify email address for Spring Boilerplate account";
        String text = "Hello!\n\n The email address for your Spring Boilerplate account needs to be verified. " +
                "Please click this confirmation link:\n\n " + emailVerificationLink +
                "\n\nIf you didn't sign up with this email address, please disregard this message.";

        sendEmail(recipientEmail, subject, text);
    }

    private void sendEmail(String recipientEmail, String subject, String text)
            throws MessagingException, UnsupportedEncodingException {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("aerisvulpe@gmail.com");
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    private GitKitProfile gitKitProfileFromAccount(Account account) {
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
