package com.roommatefinder.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
    private String email;
    private String name;
    private Long userId;
    private boolean hasProfile;
    private String role;
}
