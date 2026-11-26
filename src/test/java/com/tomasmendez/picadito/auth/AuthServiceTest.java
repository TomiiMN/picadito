package com.tomasmendez.picadito.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tomasmendez.picadito.auth.dto.AuthResponse;
import com.tomasmendez.picadito.auth.dto.LoginRequest;
import com.tomasmendez.picadito.auth.dto.RegisterRequest;
import com.tomasmendez.picadito.shared.exception.ConflictException;
import com.tomasmendez.picadito.shared.exception.ErrorCode;
import com.tomasmendez.picadito.shared.exception.InvalidCredentialsException;
import com.tomasmendez.picadito.user.Role;
import com.tomasmendez.picadito.user.User;
import com.tomasmendez.picadito.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterPlayerWithHashedPasswordAndNormalizedEmail() {
        RegisterRequest request = new RegisterRequest(
                "  Ana@Mail.com ", "secret123", "Ana Pérez", null);
        given(userRepository.existsByEmail("ana@mail.com")).willReturn(false);
        given(passwordEncoder.encode("secret123")).willReturn("hashed-password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("ana@mail.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(saved.getRole()).isEqualTo(Role.PLAYER);
    }

    @Test
    void shouldThrowConflictWhenEmailIsAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest(
                "ana@mail.com", "secret123", "Ana Pérez", null);
        given(userRepository.existsByEmail("ana@mail.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_REGISTERED);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() {
        User user = new User("ana@mail.com", "stored-hash", "Ana Pérez", null, Role.PLAYER);
        AuthResponse expected = new AuthResponse("token", "Bearer", Instant.now());
        given(userRepository.findByEmail("ana@mail.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("secret123", "stored-hash")).willReturn(true);
        given(tokenService.issueToken(user)).willReturn(expected);

        AuthResponse response = authService.login(new LoginRequest(" Ana@Mail.com ", "secret123"));

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void shouldRejectWrongPassword() {
        User user = new User("ana@mail.com", "stored-hash", "Ana Pérez", null, Role.PLAYER);
        given(userRepository.findByEmail("ana@mail.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong-password", "stored-hash")).willReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@mail.com", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(tokenService, never()).issueToken(any());
    }

    @Test
    void shouldRejectUnknownEmailAndStillCheckAPasswordHash() {
        given(userRepository.findByEmail("ghost@mail.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@mail.com", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(passwordEncoder).matches(eq("secret123"), any());
        verify(tokenService, never()).issueToken(any());
    }
}
