package dev.turkermenes.taskmanager.service;

import dev.turkermenes.taskmanager.dto.LoginRequest;
import dev.turkermenes.taskmanager.dto.LoginResponse;
import dev.turkermenes.taskmanager.dto.RegisterRequest;
import dev.turkermenes.taskmanager.dto.UserResponse;
import dev.turkermenes.taskmanager.exception.InvalidCredentialsException;
import dev.turkermenes.taskmanager.exception.UserAlreadyExistsException;
import dev.turkermenes.taskmanager.model.Role;
import dev.turkermenes.taskmanager.model.User;
import dev.turkermenes.taskmanager.repository.UserRepository;
import dev.turkermenes.taskmanager.service.AuthService;
import dev.turkermenes.taskmanager.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("register")
    class Register {

        private final RegisterRequest request =
                new RegisterRequest("ahmet", "ahmet@ornek.com", "Sifre1234");

        @Test
        @DisplayName("Şifreyi hash'leyip USER rolüyle kaydeder")
        void shouldHashPasswordAndSaveWithUserRole() {
            // given
            when(userRepository.existsByUsername("ahmet")).thenReturn(false);
            when(userRepository.existsByEmail("ahmet@ornek.com")).thenReturn(false);
            when(passwordEncoder.encode("Sifre1234")).thenReturn("HASHED_PASSWORD");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            // when
            UserResponse response = authService.register(request);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getPassword())
                    .isEqualTo("HASHED_PASSWORD")
                    .isNotEqualTo("Sifre1234");
            assertThat(savedUser.getRole()).isEqualTo(Role.USER);
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.username()).isEqualTo("ahmet");
        }

        @Test
        @DisplayName("Kullanıcı adı alınmışsa hata fırlatır ve kaydetmez")
        void whenUsernameExists_shouldThrowAndNotSave() {
            // given
            when(userRepository.existsByUsername("ahmet")).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(UserAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
            verifyNoInteractions(passwordEncoder);
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        private final User user = User.builder()
                .id(1L)
                .username("ahmet")
                .password("HASHED_PASSWORD")
                .role(Role.USER)
                .build();

        @Test
        @DisplayName("Doğru bilgilerle token döner")
        void withValidCredentials_shouldReturnToken() {
            // given
            when(userRepository.findByUsername("ahmet")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("Sifre1234", "HASHED_PASSWORD")).thenReturn(true);
            when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");
            when(jwtService.getExpirationSeconds()).thenReturn(3600L);

            // when
            LoginResponse response = authService.login(new LoginRequest("ahmet", "Sifre1234"));

            // then
            assertThat(response.accessToken()).isEqualTo("fake-jwt-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
            assertThat(response.expiresIn()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("Yanlış şifrede hata fırlatır ve token üretmez")
        void withWrongPassword_shouldThrowAndNotGenerateToken() {
            // given
            when(userRepository.findByUsername("ahmet")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("yanlis", "HASHED_PASSWORD")).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(new LoginRequest("ahmet", "yanlis")))
                    .isInstanceOf(InvalidCredentialsException.class);

            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("Olmayan kullanıcı için hata fırlatır")
        void withUnknownUser_shouldThrowInvalidCredentials() {
            // Given
            when(userRepository.findByUsername("kimseyok")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(new LoginRequest("kimseyok", "1234")))
                    .isInstanceOf(InvalidCredentialsException.class);

            verifyNoInteractions(jwtService, passwordEncoder);
        }
    }
}
