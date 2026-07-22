package com.bondhub.Assistant.dto.request;

import java.util.UUID;

import lombok.Data;

@Data
public class AssignPermissionRequest {
    private UUID roleId;
    private UUID permissionId;
}
