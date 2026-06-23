package com.career.controller;

import com.career.dto.ApiResponse;
import com.career.dto.AuthResponse;
import com.career.dto.LoginRequest;
import com.career.dto.RegisterRequest;
import com.career.dto.UserDto;
import com.career.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ApiResponse logout(@RequestHeader(value = "X-Token", required = false) String token) {
        authService.logout(token);
        return new ApiResponse("已退出登录");
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader("X-Token") String token) {
        return UserDto.from(authService.requireUser(token));
    }
}
