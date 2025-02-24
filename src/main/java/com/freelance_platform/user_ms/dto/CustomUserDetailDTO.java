package com.freelance_platform.user_ms.dto;

import com.freelance_platform.user_ms.model.Role;
import com.freelance_platform.user_ms.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CustomUserDetailDTO implements UserDetails {
    private final String username;
    private final String password;
    private final Set<? extends GrantedAuthority> authorities;

    public CustomUserDetailDTO(User byUsername) {
        this.username = byUsername.getEmail();
        this.password = byUsername.getPassword();
        Set<GrantedAuthority> auths = new HashSet<>();
        for (Role role : byUsername.getRoles()) {
            auths.add(new SimpleGrantedAuthority(role.getName()));
        }
        this.authorities = auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
