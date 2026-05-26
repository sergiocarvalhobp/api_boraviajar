package com.boraviajar.api.security;

import com.boraviajar.api.entity.User;
import com.boraviajar.api.repo.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String SESSION_COOKIE = "app_session_id";
    /** Header alternativo — alguns proxies removem Authorization em POST. */
    private static final String SESSION_HEADER = "X-App-Session";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.endsWith("/auth/token") || path.endsWith("/public/session");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String auth = resolveAuthorizationLikeHeader(request);
        Optional<String> openIdOpt = jwtService.parseOpenId(auth);
        if (openIdOpt.isPresent()) {
            Optional<User> userOpt = userRepository.findByOpenId(openIdOpt.get());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                var principal = new AuthUserDetails(user);
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn(
                        "JWT com openId válido mas sem utilizador na tabela users (mesma BD que o Node?). openId={}",
                        openIdOpt.get());
            }
        } else if (hasBearerLikeValue(auth)) {
            log.debug(
                    "Bearer presente mas JWT rejeitado (assinatura/expiração) ou sem claim openId — confira JWT_SECRET igual ao do bora_viajar (Node).");
        } else if (isMutatingMethod(request.getMethod()) && looksLikeProtectedApi(request.getRequestURI())) {
            log.warn(
                    "POST/PUT/PATCH {} sem sessão reconhecida (Authorization={}, {}={}, Cookie={}, query {}={})",
                    request.getRequestURI(),
                    request.getHeader("Authorization") != null,
                    SESSION_HEADER,
                    request.getHeader(SESSION_HEADER) != null,
                    request.getHeader("Cookie") != null);
        }

        filterChain.doFilter(request, response);
    }

    private static boolean hasBearerLikeValue(String auth) {
        return auth != null && auth.startsWith("Bearer ") && auth.length() > 7;
    }

    private static boolean isMutatingMethod(String method) {
        if (method == null) return false;
        return switch (method.toUpperCase()) {
            case "POST", "PUT", "PATCH", "DELETE" -> true;
            default -> false;
        };
    }

    private static boolean looksLikeProtectedApi(String uri) {
        return uri != null && uri.startsWith("/api/v1/")
                && !uri.startsWith("/api/v1/public/")
                && !uri.endsWith("/auth/token");
    }

    /**
     * Ordem: Authorization Bearer → X-App-Session → Cookie (header ou getCookies).
     */
    private String resolveAuthorizationLikeHeader(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth;
        }

        String sessionHeader = request.getHeader(SESSION_HEADER);
        if (sessionHeader != null && !sessionHeader.isBlank()) {
            String trimmed = sessionHeader.trim();
            return trimmed.startsWith("Bearer ") ? trimmed : "Bearer " + trimmed;
        }

        String fromCookieHeader = tokenFromCookieHeader(request.getHeader("Cookie"));
        if (fromCookieHeader != null) {
            return "Bearer " + fromCookieHeader;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (SESSION_COOKIE.equals(c.getName())) {
                    String token = c.getValue();
                    if (token != null && !token.isBlank()) {
                        return "Bearer " + token.trim();
                    }
                }
            }
        }

        return auth;
    }

    private static String tokenFromCookieHeader(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }
        for (String part : cookieHeader.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(SESSION_COOKIE + "=")) {
                String token = trimmed.substring(SESSION_COOKIE.length() + 1).trim();
                return token.isEmpty() ? null : token;
            }
        }
        return null;
    }
}
