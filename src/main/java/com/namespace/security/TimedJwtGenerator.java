package com.namespace.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.jwt.JwtConstants;
import org.pac4j.jwt.profile.JwtGenerator;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by Aaron on 22/04/2016.
 */
public class TimedJwtGenerator<U extends UserProfile> extends JwtGenerator<U> {

    public static final int LONG_ACCESS_TOKEN_TIME = 60;

    public TimedJwtGenerator(String secret) {
        super(secret);
    }

    public TimedJwtGenerator(String secret, boolean encrypted) {
        super(secret, encrypted);
    }

    public TimedJwtGenerator(String signingSecret, String encryptionSecret) {
        super(signingSecret, encryptionSecret);
    }

    @Override
    public String generate(U profile) {
        return generate(profile, LONG_ACCESS_TOKEN_TIME);
    }

    public String generate(U profile, int numOfDays) {
        CommonHelper.assertNotNull("profile", profile);
        CommonHelper.assertNull("profile.sub", profile.getAttribute(JwtConstants.SUBJECT));
        CommonHelper.assertNull("profile.iat", profile.getAttribute(JwtConstants.ISSUE_TIME));
        CommonHelper.assertNull(INTERNAL_ROLES, profile.getAttribute(INTERNAL_ROLES));
        CommonHelper.assertNull(INTERNAL_PERMISSIONS, profile.getAttribute(INTERNAL_PERMISSIONS));
        CommonHelper.assertNotBlank("signingSecret", getSigningSecret());
        CommonHelper.assertNotNull("jwsAlgorithm", getJwsAlgorithm());

        try {
            // Create HMAC signer
            final JWSSigner signer = new MACSigner(getSigningSecret());

            Date issueDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(issueDate);
            cal.add(Calendar.DATE, numOfDays); // add days
            Date expirationDate = cal.getTime();

            // Build claims
            final JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                    .subject(profile.getTypedId())
                    .issueTime(issueDate)
                    .expirationTime(expirationDate);

            // add attributes
            final Map<String, Object> attributes = profile.getAttributes();
            for (final String key : attributes.keySet()) {
                builder.claim(key, attributes.get(key));
            }
            builder.claim(INTERNAL_ROLES, profile.getRoles());
            builder.claim(INTERNAL_PERMISSIONS, profile.getPermissions());

            // claims
            final JWTClaimsSet claims = builder.build();

            // signed
            final SignedJWT signedJWT = new SignedJWT(new JWSHeader(getJwsAlgorithm()), claims);

            // Apply the HMAC
            signedJWT.sign(signer);

            if (CommonHelper.isNotBlank(getEncryptionSecret())) {
                CommonHelper.assertNotNull("jweAlgorithm", getJweAlgorithm());
                CommonHelper.assertNotNull("encryptionMethod", getEncryptionMethod());

                // Create JWE object with signed JWT as payload
                final JWEObject jweObject = new JWEObject(
                        new JWEHeader.Builder(getJweAlgorithm(), getEncryptionMethod()).contentType("JWT").build(),
                        new Payload(signedJWT));

                // Perform encryption
                jweObject.encrypt(new DirectEncrypter(getEncryptionSecret().getBytes("UTF-8")));

                // Serialise to JWE compact form
                return jweObject.serialize();
            }
            return signedJWT.serialize();

        } catch (final Exception e) {
            throw new TechnicalException("Cannot generate JWT", e);
        }
    }
}
