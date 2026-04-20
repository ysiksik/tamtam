package com.tamtam.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tamtam.api.global.jwt.AuthPrincipal;
import com.tamtam.api.global.jwt.JwtUtils;
import com.tamtam.api.service.dto.TokenResult;
import com.tamtam.api.service.dto.UserRegisterCommand;
import com.tamtam.core.domain.entity.User;
import com.tamtam.core.domain.repository.UserRepository;
import java.util.Optional;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    @Test
    void signup_shouldSucceed_whenEmailIsUnique() {
        UserRegisterCommand command = Instancio.create(UserRegisterCommand.class);
        User mockUser = User.builder().email(command.getEmail()).build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        userService.signup(command);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_shouldFail_whenEmailExists() {
        UserRegisterCommand command = Instancio.create(UserRegisterCommand.class);
        when(userRepository.findByEmail(command.getEmail())).thenReturn(Optional.of(User.builder()
                                                                                        .id(1L)
                                                                                        .email("user@example.com")
                                                                                        .password("test")
                                                                                        .build()));

        assertThatThrownBy(() -> userService.signup(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이미 사용 중인 이메일");
    }

    @Test
    void login_shouldReturnTokenResult_whenAuthenticated() {
        String email = "user@example.com";
        String password = "Password123";

        AuthPrincipal principal = Instancio.create(AuthPrincipal.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        TokenResult result = userService.login(email, password);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    void reissueAccessToken_shouldReturnTokenResult_whenValidToken() {
        String refreshToken = JwtUtils.createRefreshToken(Instancio.create(AuthPrincipal.class));
        User user = User.builder().id(1L).email("user@example.com").build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        TokenResult result = userService.reissueAccessToken(refreshToken);

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    void reissueAccessToken_shouldFail_whenUserNotFound() {
        String refreshToken = JwtUtils.createRefreshToken(Instancio.create(AuthPrincipal.class));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.reissueAccessToken(refreshToken))
            .isInstanceOf(RuntimeException.class);
    }
}
