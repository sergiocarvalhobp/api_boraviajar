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
        } else if (auth != null && auth.startsWith("Bearer ") && auth.length() > 7) {
            String token = auth.substring(7).trim();
            if (!token.isEmpty()) {
                log.debug(
                        "Bearer presente mas JWT rejeitado (assinatura/expiração) ou sem claim openId — confira JWT_SECRET igual ao do bora_viajar (Node).");
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Primeiro tenta Authorization: Bearer.
     * Se a borda/nginx remover esse header, usa cookie app_session_id como fallback.
     */
    private String resolveAuthorizationLikeHeader(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) return auth;
        for (Cookie c : cookies) {
            if (SESSION_COOKIE.equals(c.getName())) {
                String token = c.getValue();
                if (token != null && !token.isBlank()) {
                    return "Bearer " + token.trim();
                }
            }
        }
        return auth;
    }
}
