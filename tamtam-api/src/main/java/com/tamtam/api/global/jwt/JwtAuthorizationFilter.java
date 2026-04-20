package com.tamtam.api.global.jwt;



import com.tamtam.core.domain.entity.User;
import com.tamtam.core.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT를 이용한 인증
 */
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public JwtAuthorizationFilter(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // "Bearer " 제거
            try {
                Authentication authentication = getUsernamePasswordAuthenticationToken(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Handle token validation failure
                deleteJwtTokenCookie(response);
            }
        }

        chain.doFilter(request, response);
    }


    /**
     * JWT 토큰으로 User를 찾아서 UsernamePasswordAuthenticationToken를 만들어서 반환한다.
     * User가 없다면 null
     */
    private Authentication getUsernamePasswordAuthenticationToken(String token) {
        String userId = JwtUtils.getUserId(token);
        if (userId != null) {
            User user = userRepository.findByEmail(userId)
                                      .orElseThrow(() -> new UsernameNotFoundException("토큰 사용자 없음: " + userId));
            if (user != null && !isRefreshToken(token)) {
                AuthPrincipal authPrincipal = AuthPrincipal.from(user);
                return new UsernamePasswordAuthenticationToken(authPrincipal, null, authPrincipal.getAuthorities());
            }
        }
        return null;
    }

    private boolean isRefreshToken(String token) {
        Claims claims = Jwts.parserBuilder()
                            .setSigningKeyResolver(SigningKeyResolver.INSTANCE)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
        return claims.containsKey("isRefreshToken") && claims.get("isRefreshToken", Boolean.class);
    }
    private void deleteJwtTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(HttpHeaders.AUTHORIZATION, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
