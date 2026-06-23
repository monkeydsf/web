package com.career.service;

import com.career.dto.AuthResponse;
import com.career.dto.LoginRequest;
import com.career.dto.RegisterRequest;
import com.career.dto.UserDto;
import com.career.model.Role;
import com.career.model.User;
import com.career.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        Role role = request.role() == Role.COMPANY ? Role.COMPANY : Role.JOB_SEEKER;

        User user = new User();
        user.setUsername(request.username().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setMajor(request.major());
        user.setGraduationYear(request.graduationYear());
        user.setRole(role);
        userRepository.save(user);
        return createSession(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return createSession(user);
    }

    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            sessions.remove(token);
        }
    }

    public User requireUser(String token) {
        if (!StringUtils.hasText(token) || !sessions.containsKey(token)) {
            throw new SecurityException("Please login first");
        }
        return userRepository.findById(sessions.get(token))
                .orElseThrow(() -> new SecurityException("Login session expired"));
    }

    public User requireAdmin(String token) {
        User user = requireUser(token);
        if (user.getRole() != Role.ADMIN) {
            throw new SecurityException("Admin permission required");
        }
        return user;
    }

    public User requireEmployer(String token) {
        User user = requireUser(token);
        if (user.getRole() != Role.COMPANY && user.getRole() != Role.ADMIN) {
            throw new SecurityException("Company or admin permission required");
        }
        return user;
    }

    public User requireStudent(String token) {
        User user = requireUser(token);
        if (user.getRole() != Role.JOB_SEEKER && user.getRole() != Role.STUDENT) {
            throw new SecurityException("Student permission required");
        }
        return user;
    }

    public BCryptPasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }

    private AuthResponse createSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getId());
        return new AuthResponse(token, UserDto.from(user));
    }
}
