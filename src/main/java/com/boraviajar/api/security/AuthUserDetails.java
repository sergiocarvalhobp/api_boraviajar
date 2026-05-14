package com.boraviajar.api.security;

import com.boraviajar.api.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/** Utilizador autenticado via JWT (mesmo token que o Express assina com JWT_SECRET). */
public record AuthUserDetails(User user) implements UserDetails {

    @Override
    public String getUsername() {
        return String.valueOf(user.getId());
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        String r = user.getRole() != null ? user.getRole() : "user";
        return List.of(new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
