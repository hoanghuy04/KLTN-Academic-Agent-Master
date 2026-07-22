package com.bondhub.Assistant.dto.response;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private long refreshTokenExpirationMs;
    private String email;
    private String code;
    private List<UserProfileResponse> profiles;
}