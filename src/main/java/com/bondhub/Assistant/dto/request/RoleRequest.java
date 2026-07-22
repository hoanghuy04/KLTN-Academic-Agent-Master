package com.bondhub.Assistant.dto.request;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoleRequest {
    private String name;
    private Boolean isSystemRole;
    private String description;
    private Boolean isActive;
    private List<UUID> permissionIds;
}
