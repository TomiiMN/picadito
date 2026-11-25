package com.tomasmendez.picadito.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tomasmendez.picadito.auth.dto.RegisterRequest;
import com.tomasmendez.picadito.shared.exception.ConflictException;
import com.tomasmendez.picadito.shared.exception.ErrorCode;
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

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
}
