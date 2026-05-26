package com.boraviajar.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * Permite ler o JWT no corpo JSON em POST (nginx às vezes remove Authorization em mutações).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestBodyCacheFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (shouldWrap(request)) {
            filterChain.doFilter(new ContentCachingRequestWrapper(request), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private static boolean shouldWrap(HttpServletRequest request) {
        String method = request.getMethod();
        if (method == null) {
            return false;
        }
        return switch (method.toUpperCase()) {
            case "POST", "PUT", "PATCH" -> {
                String ct = request.getContentType();
                yield ct != null && ct.toLowerCase().contains("application/json");
            }
            default -> false;
        };
    }
}
