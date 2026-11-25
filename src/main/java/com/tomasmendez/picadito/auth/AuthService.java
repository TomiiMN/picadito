package com.tomasmendez.picadito.auth;

import java.util.Locale;

import com.tomasmendez.picadito.auth.dto.RegisterRequest;
import com.tomasmendez.picadito.shared.exception.ConflictException;
import com.tomasmendez.picadito.shared.exception.ErrorCode;
import com.tomasmendez.picadito.user.Role;
import com.tomasmendez.picadito.user.User;
import com.tomasmendez.picadito.user.UserRepository;
import com.tomasmendez.picadito.user.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED,
                    "Email is already registered");
        }

        User user = new User(
                email,
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                request.phone(),
                Role.PLAYER);

        User saved = userRepository.save(user);
        log.info("New player registered with id {}", saved.getId());
        return UserResponse.from(saved);
    }
}
