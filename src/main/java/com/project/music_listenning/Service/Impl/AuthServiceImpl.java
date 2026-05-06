package com.project.music_listenning.Service.Impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.music_listenning.Entity.User;
import com.project.music_listenning.Repository.UserRepository;
import com.project.music_listenning.Service.AuthService;
import com.project.music_listenning.dto.request.AuthRequest;
import com.project.music_listenning.dto.response.AuthDTO.*;
import com.project.music_listenning.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Đăng ký tài khoản mới.
     * Validate trùng email/username → hash password → lưu DB → trả token.
     */
    @Transactional
    @Override
    public AuthResponse register(AuthRequest.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username đã được sử dụng");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(User.Role.USER)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    /**
     * Đăng nhập.
     * AuthenticationManager tự gọi UserDetailsService + PasswordEncoder để verify.
     * Nếu sai → throws BadCredentialsException → GlobalExceptionHandler xử lý.
     */
    @Override
    public AuthResponse login(AuthRequest.LoginRequest request) {
        // Spring Security tự kiểm tra email + password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        return buildAuthResponse(user);
    }

    /**
     * Refresh access token — dùng khi access token hết hạn.
     * Client gửi refresh token → validate → tạo access token mới.
     */
    @Override
    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        return buildAuthResponse(user);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                new UserInfo(
                        user.getId().toString(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getAvatarUrl(),
                        user.getRole().name()
                )
        );
    }
}
