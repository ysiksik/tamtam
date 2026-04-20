package com.tamtam.api.service;

import com.tamtam.api.global.jwt.AuthPrincipal;
import com.tamtam.api.global.jwt.JwtUtils;
import com.tamtam.api.service.dto.TokenResult;
import com.tamtam.api.service.dto.UserRegisterCommand;
import com.tamtam.core.domain.entity.User;
import com.tamtam.core.domain.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void signup(@Valid UserRegisterCommand command) {
        if (userRepository.findByEmail(command.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User entity = command.toEntity(passwordEncoder);
        userRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public TokenResult login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return tokenMakeService(principal);
    }

    @Transactional(readOnly = true)
    public TokenResult reissueAccessToken(String refreshToken) {
        User user = userRepository.findByEmail(JwtUtils.getUserId(refreshToken))
                                  .orElseThrow(() -> new UsernameNotFoundException("유효하지 않은 사용자입니다."));

        AuthPrincipal authPrincipal = AuthPrincipal.from(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            authPrincipal,
            null,
            authPrincipal.getAuthorities()
        );
        return tokenMakeService((AuthPrincipal) authentication.getPrincipal());
    }
    public TokenResult tokenMakeService(final AuthPrincipal authPrincipal) {
        String token = JwtUtils.createToken(authPrincipal);
        String refreshToken = JwtUtils.createRefreshToken(authPrincipal);
        return new TokenResult(token, refreshToken);
    }


}
