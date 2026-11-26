package com.tomasmendez.picadito.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import com.tomasmendez.picadito.auth.dto.AuthResponse;
import com.tomasmendez.picadito.shared.exception.ConflictException;
import com.tomasmendez.picadito.shared.exception.ErrorCode;
import com.tomasmendez.picadito.shared.exception.InvalidCredentialsException;
import com.tomasmendez.picadito.user.Role;
import com.tomasmendez.picadito.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    private static final String VALID_REQUEST = """
            {"email": "ana@mail.com", "password": "secret123", "fullName": "Ana Pérez"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void shouldReturn201WithoutExposingThePassword() throws Exception {
        given(authService.register(any())).willReturn(new UserResponse(
                1L, "ana@mail.com", "Ana Pérez", null, Role.PLAYER, Instant.now()));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@mail.com"))
                .andExpect(jsonPath("$.role").value("PLAYER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldReturn400WhenPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "ana@mail.com", "password": "short", "fullName": "Ana Pérez"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("password"));

        verifyNoInteractions(authService);
    }

    @Test
    void shouldReturn409WhenEmailIsAlreadyRegistered() throws Exception {
        given(authService.register(any())).willThrow(new ConflictException(
                ErrorCode.EMAIL_ALREADY_REGISTERED, "Email is already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void shouldReturn200WithTokenWhenLoginSucceeds() throws Exception {
        given(authService.login(any())).willReturn(
                new AuthResponse("jwt-token", "Bearer", Instant.parse("2026-12-01T12:00:00Z")));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "ana@mail.com", "password": "secret123"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    void shouldReturn401WithGenericMessageWhenCredentialsAreInvalid() throws Exception {
        given(authService.login(any())).willThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "ana@mail.com", "password": "wrong-password"}
                            """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }
}
