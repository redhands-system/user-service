package com.redhands.user_service.service;

import com.redhands.user_service.dto.LoginRequest;
import com.redhands.user_service.dto.SignupRequest;
import com.redhands.user_service.entity.User;
import com.redhands.user_service.repository.UserRepository;
import com.redhands.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입
     */
    @Transactional
    public User signup(SignupRequest request) {
        // 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("이미 존재하는 사용자명입니다");
        }

        // User 생성 (비밀번호 평문 저장 - 실제로는 BCrypt 사용)
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRole("USER");  // 기본 권한

        return userRepository.save(user);
    }

    /**
     * 로그인 (JWT 발급)
     */
    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        // 비밀번호 확인 (평문 비교 - 실제로는 BCrypt)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        // JWT 토큰 생성
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