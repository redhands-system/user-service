package com.redhands.user_service.controller;

import com.redhands.user_service.dto.LoginRequest;
import com.redhands.user_service.dto.LoginResponse;
import com.redhands.user_service.dto.RefreshTokenRequest;
import com.redhands.user_service.dto.SignupRequest;
import com.redhands.user_service.entity.User;
import com.redhands.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            User user = userService.signup(request);
            return ResponseEntity.ok("회원가입 성공: " + user.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Map<String, String> tokens = userService.login(request);

            LoginResponse response = new LoginResponse(
                    tokens.get("accessToken"),
                    tokens.get("refreshToken"),
                    "로그인 성공"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String newAccessToken = userService.refreshAccessToken(request.getRefreshToken());

            Map<String, String> response = Map.of(
                    "accessToken", newAccessToken,
                    "message", "Access Token 갱신 성공"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}