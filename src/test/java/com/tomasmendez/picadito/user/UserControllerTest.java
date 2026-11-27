package com.tomasmendez.picadito.user;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import com.tomasmendez.picadito.auth.WebSecurityTestConfig;
import com.tomasmendez.picadito.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(WebSecurityTestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnTheUserIdentifiedByTheTokenSubject() throws Exception {
        given(userService.getById(7L)).willReturn(new UserResponse(
                7L, "ana@mail.com", "Ana Pérez", null, Role.PLAYER, Instant.now()));

        mockMvc.perform(get("/api/users/me").with(jwt().jwt(token -> token.subject("7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("ana@mail.com"));
    }

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
