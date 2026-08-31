package com.hlima.erp.auth.service;

import com.hlima.erp.auth.dto.AuthResponse;
import com.hlima.erp.auth.dto.LoginRequest;
import com.hlima.erp.auth.dto.RegisterUserRequest;
import com.hlima.erp.auth.dto.UserResponse;
import com.hlima.erp.auth.entity.User;
import com.hlima.erp.auth.repository.UserRepository;
import com.hlima.erp.auth.security.JwtService;
import com.hlima.erp.shared.exception.ConflictException;
import com.hlima.erp.shared.exception.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                // Mesma mensagem pra e-mail inexistente e senha errada — evita
                // que alguém descubra quais e-mails têm conta por tentativa e erro.
                .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas."));

        if (user.isLocked()) {
            throw new UnauthorizedException(
                    "Conta temporariamente bloqueada por excesso de tentativas. Tente novamente mais tarde.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            registerFailedAttempt(user);
            throw new UnauthorizedException("Credenciais inválidas.");
        }

        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        return issueTokens(user);
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(Instant.now().plus(LOCKOUT_MINUTES, ChronoUnit.MINUTES));
        } else {
            user.setFailedLoginAttempts(attempts);
        }
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        try {
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new UnauthorizedException("Token de renovação inválido.");
            }

            var userId = jwtService.extractUserId(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("Sessão inválida. Faça login novamente."));

            if (!refreshToken.equals(user.getRefreshToken())) {
                throw new UnauthorizedException("Sessão inválida. Faça login novamente.");
            }

            return issueTokens(user);
        } catch (JwtException ex) {
            throw new UnauthorizedException("Sessão expirada. Faça login novamente.");
        }
    }

    @Transactional
    public void logout(User user) {
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Já existe um usuário com este e-mail.");
        }

        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.role()
        );

        return UserResponse.from(userRepository.save(user));
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthResponse(accessToken, refreshToken, UserResponse.from(user));
    }
}
