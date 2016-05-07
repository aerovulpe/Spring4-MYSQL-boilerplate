package com.namespace.web;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Enforce Https for Heroku
 */
public class HttpsEnforcer implements Filter {

    private static final String X_FORWARDED_PROTO = "x-forwarded-proto";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String headerXForwarded = request.getHeader(X_FORWARDED_PROTO);
        if (headerXForwarded != null && (!headerXForwarded.contains("https"))) {
            String url = "https://" + request.getServerName();
            if (request.getPathInfo() != null) {
                url += request.getPathInfo();
            }
            if (request.getQueryString() != null) {
                url += request.getQueryString();
            }
            response.sendRedirect(url);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        // nothing
    }
}