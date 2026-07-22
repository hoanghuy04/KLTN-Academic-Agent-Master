package com.bondhub.Assistant.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileResponse {
    private UUID userId;
    private String fullName;
    private String avatarUrl;
    private String role;
    private String roleDescription;
    /** True when this profile belongs to a system-defined admin role */
    private Boolean isSystemRole;
    /** Permission names assigned to this profile's role — used for UI visibility */
    private List<PermissionInfo> permissions;
}
