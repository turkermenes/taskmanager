package com.ornek.taskmanager.service;

import com.ornek.taskmanager.dto.LoginRequest;
import com.ornek.taskmanager.dto.LoginResponse;
import com.ornek.taskmanager.dto.RegisterRequest;
import com.ornek.taskmanager.dto.UserResponse;
import com.ornek.taskmanager.exception.InvalidCredentialsException;
import com.ornek.taskmanager.exception.UserAlreadyExistsException;
import com.ornek.taskmanager.model.Role;
import com.ornek.taskmanager.model.User;
import com.ornek.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Bu kullanıcı adı zaten alınmış: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Bu e-posta adresi zaten kayıtlı");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);
        log.info("Yeni kullanıcı kaydedildi. id={}, username={}", saved.getId(), saved.getUsername());
        return UserResponse.from(saved);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        log.info("Kullanıcı giriş yaptı. username={}", user.getUsername());
        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }
}