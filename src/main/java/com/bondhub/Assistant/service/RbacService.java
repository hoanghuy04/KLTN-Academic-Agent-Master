package com.bondhub.Assistant.service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import com.bondhub.Assistant.dto.response.PageResponse;
import com.bondhub.Assistant.dto.request.PermissionRequest;
import com.bondhub.Assistant.dto.request.RoleRequest;
import com.bondhub.Assistant.dto.response.RoleResponse;
import com.bondhub.Assistant.dto.response.PermissionResponse;

public interface RbacService {
    // --- ROLES ---
    RoleResponse createRole(RoleRequest request);
    RoleResponse updateRole(UUID roleId, RoleRequest request);
    void deleteRole(UUID roleId);
    void recoverRole(UUID roleId);
    void deleteResources(List<UUID> roleIds);
    void recoverResources(List<UUID> roleIds);
    RoleResponse getRoleDetails(UUID roleId);
    PageResponse<List<RoleResponse>> getAllRoles(Pageable pageable);

    // --- PERMISSIONS ---
    PermissionResponse createPermission(PermissionRequest request);
    PermissionResponse updatePermission(UUID permissionId, PermissionRequest request);
    void deletePermission(UUID permissionId);
    void recoverPermission(UUID permissionId);
    void deletePermissionResources(List<UUID> permissionIds);
    void recoverPermissionResources(List<UUID> permissionIds);
    PageResponse<List<PermissionResponse>> getAllPermissions(Pageable pageable);

    // --- ASSIGNMENTS ---
    void assignPermissionToRole(UUID roleId, UUID permissionId);
}
