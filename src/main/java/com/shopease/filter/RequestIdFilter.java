package com.shopease.filter;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

/**
 * Filter that generates a unique Request ID for every incoming HTTP request,
 * attaches it to SLF4J MDC for structured correlation logging, and returns it in the X-Request-ID header.
 */
@WebFilter(filterName = "RequestIdFilter", urlPatterns = "/*")
public class RequestIdFilter implements Filter {
    private static final String MDC_KEY = "requestId";
    private static final String HEADER_NAME = "X-Request-ID";

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization required
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String requestId = req.getHeader(HEADER_NAME);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_KEY, requestId);
        resp.setHeader(HEADER_NAME, requestId);
        req.setAttribute(MDC_KEY, requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    @Override
    public void destroy() {
        // No destruction required
    }
}
