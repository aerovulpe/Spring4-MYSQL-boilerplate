package com.namespace.security;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.*;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.jwt.JwtConstants;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.jwt.profile.JwtProfile;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Aaron on 22/04/2016.
 */
public class TimedJwtAuthenticator extends JwtAuthenticator {

    public TimedJwtAuthenticator() {
    }

    public TimedJwtAuthenticator(String signingSecret) {
        super(signingSecret);
    }

    public TimedJwtAuthenticator(String signingSecret, String encryptionSecret) {
        super(signingSecret, encryptionSecret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final TokenCredentials credentials) {
        CommonHelper.assertNotBlank("signingSecret", getSigningSecret());

        final String token = credentials.getToken();
        boolean verified;
        SignedJWT signedJWT;

        try {
            // Parse the token
            final JWT jwt = JWTParser.parse(token);

            if (jwt instanceof SignedJWT) {
                signedJWT = (SignedJWT) jwt;
            } else if (jwt instanceof EncryptedJWT) {
                CommonHelper.assertNotBlank("encryptionSecret", getEncryptionSecret());

                final JWEObject jweObject = (JWEObject) jwt;
                jweObject.decrypt(new DirectDecrypter(getEncryptionSecret().getBytes("UTF-8")));

                // Extract payload
                signedJWT = jweObject.getPayload().toSignedJWT();
            } else {
                throw new TechnicalException("unsupported unsecured jwt");
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            verified = claimsSet.getExpirationTime().compareTo(new Date()) > 0 &&
                    signedJWT.verify(new MACVerifier(getSigningSecret()));
        } catch (final Exception e) {
            throw new TechnicalException("Cannot decrypt / verify JWT", e);
        }

        if (!verified) {
            final String message = "JWT verification failed: " + token;
            throw new CredentialsException(message);
        }

        try {
            createJwtProfile(credentials, signedJWT);
        } catch (final Exception e) {
            throw new TechnicalException("Cannot get claimSet", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void createJwtProfile(final TokenCredentials credentials, final SignedJWT signedJWT) throws ParseException {
        final JWTClaimsSet claimSet = signedJWT.getJWTClaimsSet();
        String subject = claimSet.getSubject();

        if (!subject.contains(UserProfile.SEPARATOR)) {
            subject = JwtProfile.class.getSimpleName() + UserProfile.SEPARATOR + subject;
        }

        final Map<String, Object> attributes = new HashMap<>(claimSet.getClaims());
        attributes.remove(JwtConstants.SUBJECT);
        final List<String> roles = (List<String>) attributes.get(JwtGenerator.INTERNAL_ROLES);
        attributes.remove(JwtGenerator.INTERNAL_ROLES);
        final List<String> permissions = (List<String>) attributes.get(JwtGenerator.INTERNAL_PERMISSIONS);
        attributes.remove(JwtGenerator.INTERNAL_PERMISSIONS);
        final CommonProfile profile = ProfileHelper.buildProfile(subject, attributes);

        if (roles != null) {
            profile.addRoles(roles);
        }
        if (permissions != null) {
            profile.addPermissions(permissions);
        }
        credentials.setUserProfile(profile);
    }
}
