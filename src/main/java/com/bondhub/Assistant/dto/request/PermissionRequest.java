package com.bondhub.Assistant.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PermissionRequest {
    private String name;
    private Integer accessLevel;
    private Boolean isActive;
}
