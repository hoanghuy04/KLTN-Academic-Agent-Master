package com.bondhub.Assistant.dto.response;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SelectProfileResponse {
    private String accessToken;
    private String refreshToken;
    private long refreshTokenExpirationMs;
    private String email;
    private String fullName;
    private String role;
    private String code;
    private String avatarUrl;
    private Boolean isSystemRole;
    private List<PermissionInfo> permissions;
}
