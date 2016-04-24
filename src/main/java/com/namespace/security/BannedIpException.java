package com.namespace.security;

import org.pac4j.core.exception.CredentialsException;

/**
 * Created by Aaron on 24/04/2016.
 */
public class BannedIpException extends CredentialsException {
    public BannedIpException() {
        super("The IP address used to make this request has been banned");
    }
}
