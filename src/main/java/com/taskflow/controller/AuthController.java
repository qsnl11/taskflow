package com.taskflow.controller;

import com.taskflow.config.JwtService;
import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.entity.User;
import com.taskflow.security.CustomUserDetailsService;
import com.taskflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);

        User user = userService.findById(
                userService.getAllUsers().stream()
                        .filter(u -> u.getUsername().equals(request.getUsername()))
                        .findFirst()
                        .map(u -> u.getId())
                        .orElse(null)
        );

        String role = user.getRole() != null ? user.getRole().getName() : "ROLE_USER";

        return ResponseEntity.ok(new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                role
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());

        User user = userService.registerUser(request);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        String role = user.getRole() != null ? user.getRole().getName() : "ROLE_USER";

        return ResponseEntity.ok(new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                role
        ));
    }
}