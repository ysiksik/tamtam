package com.tamtam.api.global.config;


import com.tamtam.api.global.jwt.AuthPrincipal;
import java.util.Objects;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class JpaConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
                             .map(SecurityContext::getAuthentication)
                             .filter(Authentication::isAuthenticated)
                             .map(Authentication::getPrincipal)
                             .filter(principal -> !Objects.equals(principal, "anonymousUser"))
                             .map(AuthPrincipal.class::cast)
                             .map(AuthPrincipal::userId);
    }
}
