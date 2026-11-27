package com.tomasmendez.picadito.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import com.tomasmendez.picadito.auth.dto.AuthResponse;
import com.tomasmendez.picadito.user.Role;
import com.tomasmendez.picadito.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

class TokenServiceTest {

    private static final String TEST_SECRET = "cGljYWRpdG8tdGVzdC1zZWNyZXQtb25seS1mb3ItdW5pdC10ZXN0cyE=";

    private final JwtConfig jwtConfig = new JwtConfig();
    private final JwtProperties properties = new JwtProperties(TEST_SECRET, Duration.ofHours(1));
    private final TokenService tokenService = new TokenService(
            jwtConfig.jwtEncoder(jwtConfig.jwtSecretKey(properties)), properties);
    private final JwtDecoder decoder = jwtConfig.jwtDecoder(jwtConfig.jwtSecretKey(properties));

    @Test
    void shouldIssueSignedTokenWithSubjectRoleAndExpiration() {
        User user = new User("ana@mail.com", "hash", "Ana Pérez", null, Role.PLAYER);
        ReflectionTestUtils.setField(user, "id", 7L);

        AuthResponse response = tokenService.issueToken(user);
        Jwt jwt = decoder.decode(response.accessToken());

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString(TokenService.ROLE_CLAIM)).isEqualTo("PLAYER");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(1));
        assertThat(jwt.getExpiresAt()).isEqualTo(response.expiresAt());
    }
}
