package com.sms.controller;

import com.sms.dto.AppDTO;
import com.sms.dto.AuthDTO;
import com.sms.entity.User;
import com.sms.repository.UserRepository;
import com.sms.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(new AppDTO.MessageResponse("Invalid credentials"));
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        String token = jwtUtil.generateToken(userDetails, user.getRole().name());

        return ResponseEntity.ok(AuthDTO.LoginResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDTO.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new AppDTO.MessageResponse("Email already registered"));
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : User.Role.STUDENT)
                .rollNumber(request.getRollNumber())
                .branch(request.getBranch())
                .semester(request.getSemester())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .build();

        userRepository.save(user);
        return ResponseEntity.ok(new AppDTO.MessageResponse("User registered successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(AppDTO.UserResponse.from(user));
    }
}
