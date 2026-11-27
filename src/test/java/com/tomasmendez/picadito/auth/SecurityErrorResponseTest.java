package com.tomasmendez.picadito.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SecurityErrorResponsesTest.TestController.class)
@Import({WebSecurityTestConfig.class, SecurityErrorResponsesTest.TestController.class})
class SecurityErrorResponsesTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {

        @GetMapping("/test/secured")
        public String secured() {
            return "ok";
        }

        @GetMapping("/test/admin")
        @PreAuthorize("hasRole('ADMIN')")
        public String adminOnly() {
            return "ok";
        }
    }

    @Test
    void shouldReturn401InProblemDetailFormatWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/test/secured"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn401WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/test/secured")
                        .header("Authorization", "Bearer not.a.valid.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldAllowAccessWithValidToken() throws Exception {
        mockMvc.perform(get("/test/secured").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403InProblemDetailFormatWhenRoleIsNotAllowed() throws Exception {
        mockMvc.perform(get("/test/admin")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PLAYER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldAllowAdminEndpointForAdminRole() throws Exception {
        mockMvc.perform(get("/test/admin")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }
}
