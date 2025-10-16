package com.redhands.user_service.service;

import com.redhands.user_service.entity.RefreshToken;
import com.redhands.user_service.entity.User;
import com.redhands.user_service.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration:604800000}") // 7일 (밀리초)
    private long refreshTokenDuration;

    /**
     * Refresh Token 생성
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // 기존 Refresh Token 삭제
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDuration));

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Refresh Token 검증
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
        }
        return token;
    }

    /**
     * Refresh Token으로 조회
     */
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 Refresh Token입니다."));
    }
}