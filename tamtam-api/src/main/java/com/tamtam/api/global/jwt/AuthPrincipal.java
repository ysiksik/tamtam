package com.tamtam.api.global.jwt;


import com.tamtam.core.domain.entity.User;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


public record AuthPrincipal(

    Long userId,

    String email,

    String password,

    String authority

) implements UserDetails {


    public static AuthPrincipal of(Long userId, String email, String password) {
        return new AuthPrincipal(userId, email, password, "");
    }

    public static AuthPrincipal from(User entity) {
        return AuthPrincipal.of(
            entity.getId(),
            entity.getEmail(),
            entity.getPassword()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton((GrantedAuthority) () -> authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
