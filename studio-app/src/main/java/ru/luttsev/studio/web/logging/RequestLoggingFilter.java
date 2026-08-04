package ru.luttsev.studio.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Assigns a correlation id to every request so log lines produced while
 * handling it can be grepped together, and records an access log entry.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    static final String REQUEST_ID_HEADER = "X-Request-Id";
    static final String REQUEST_ID_MDC_KEY = "requestId";

    private static final String INCOMING_FORMAT = "--> {} {}";
    private static final String OUTCOME_FORMAT = "<-- {} {} {} ({} ms)";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        String uri = requestUri(request);
        long startedAt = System.nanoTime();
        log.debug(INCOMING_FORMAT, request.getMethod(), uri);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            logOutcome(request, response, uri, durationMs);
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    private void logOutcome(
            HttpServletRequest request,
            HttpServletResponse response,
            String uri,
            long durationMs) {
        int status = response.getStatus();
        String method = request.getMethod();
        if (status >= 500) {
            log.error(OUTCOME_FORMAT, method, uri, status, durationMs);
        } else if (status >= 400) {
            log.warn(OUTCOME_FORMAT, method, uri, status, durationMs);
        } else {
            log.debug(OUTCOME_FORMAT, method, uri, status, durationMs);
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String incoming = request.getHeader(REQUEST_ID_HEADER);
        return incoming == null || incoming.isBlank()
                ? UUID.randomUUID().toString()
                : incoming;
    }

    private String requestUri(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return queryString == null
                ? request.getRequestURI()
                : request.getRequestURI() + "?" + queryString;
    }
}
