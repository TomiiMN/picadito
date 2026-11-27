package com.tomasmendez.picadito.auth;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        SecurityConfig.class,
        JwtConfig.class,
        ProblemDetailAuthenticationEntryPoint.class,
        ProblemDetailAccessDeniedHandler.class
})
public class WebSecurityTestConfig {
}
