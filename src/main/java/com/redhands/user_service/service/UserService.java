package com.redhands.user_service.service;

import com.redhands.user_service.dto.LoginRequest;
import com.redhands.user_service.dto.SignupRequest;
import com.redhands.user_service.entity.RefreshToken;
import com.redhands.user_service.entity.User;
import com.redhands.user_service.repository.UserRepository;
import com.redhands.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public User signup(SignupRequest request) {
        // 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("이미 존재하는 사용자명입니다");
        }

        // User 생성
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // 암호화
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        return userRepository.save(user);
    }

    /**
     * 로그인 (Access Token + Refresh Token 발급) (암호화된 비밀번호 비교)
     */
    public Map<String, String> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        // 비밀번호 확인 (암호화된 것과 비교)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        // Access Token 생성
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole());

        // Refresh Token 생성
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken.getToken());

        return tokens;
    }

    /**
     * Refresh Token으로 Access Token 갱신
     */
    @Transactional
    public String refreshAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr);

        // 만료 확인
        refreshTokenService.verifyExpiration(refreshToken);

        // 새 Access Token 생성
        User user = refreshToken.getUser();
        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }

    /**
     * 사용자 조회
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
    }
}