package com.sms.dto;

import com.sms.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class AuthDTO {

    @Getter @Setter
    public static class LoginRequest {
        @Email @NotBlank
        private String email;
        @NotBlank
        private String password;
    }

    @Getter @Setter @Builder
    public static class LoginResponse {
        private String token;
        private String role;
        private Long userId;
        private String name;
        private String email;
    }

    @Getter @Setter
    public static class RegisterRequest {
        @NotBlank private String name;
        @Email @NotBlank private String email;
        @NotBlank private String password;
        private User.Role role;
        // Student fields
        private String rollNumber;
        private String branch;
        private Integer semester;
        // Faculty fields
        private String department;
        private String designation;
    }
}
