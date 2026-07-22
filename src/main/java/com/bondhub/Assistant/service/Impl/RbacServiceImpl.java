package com.bondhub.Assistant.service.Impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bondhub.Assistant.dto.request.*;
import com.bondhub.Assistant.dto.response.*;
import com.bondhub.Assistant.entity.*;
import com.bondhub.Assistant.repository.PermissionRepository;
import com.bondhub.Assistant.repository.RolePermissionRepository;
import com.bondhub.Assistant.repository.RoleRepository;
import com.bondhub.Assistant.service.RbacService;
import com.bondhub.Assistant.exception.AppException;
import com.bondhub.Assistant.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    // --- ROLES ---

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        
        Role role = Role.builder()
                .name(request.getName())
                .isSystemRole(request.getIsSystemRole())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        role = roleRepository.save(role);

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
            
            // Filter: keep only the highest access level for each permission name
            List<UUID> filteredIds = permissions.stream()
                .collect(Collectors.toMap(
                    Permission::getName,
                    p -> p,
                    (p1, p2) -> {
                        Integer l1 = p1.getAccessLevel();
                        Integer l2 = p2.getAccessLevel();
                        return (l1 != null ? l1 : 0) >= (l2 != null ? l2 : 0) ? p1 : p2;
                    }
                ))
                .values().stream()
                .map(Permission::getId)
                .collect(Collectors.toList());

            for (UUID pId : filteredIds) {
                assignPermissionToRole(role.getId(), pId);
            }
        }

        return getRoleDetails(role.getId());
    }

    @Override
    @Transactional
    public RoleResponse updateRole(UUID roleId, RoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        
        if (request.getName() != null && !request.getName().equals(role.getName())) {
            if (roleRepository.findByName(request.getName()).isPresent()) {
                throw new AppException(ErrorCode.ROLE_EXISTED);
            }
            role.setName(request.getName());
        }
        role.setIsSystemRole(request.getIsSystemRole());
        role.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            role.setIsActive(request.getIsActive());
        }

        roleRepository.save(role);

        if (request.getPermissionIds() != null) {
            // Clear existing and re-assign
            rolePermissionRepository.deleteByRoleId(roleId);
            
            List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
            
            // Filter: keep only the highest access level for each permission name
            List<UUID> filteredIds = permissions.stream()
                .collect(Collectors.toMap(
                    Permission::getName,
                    p -> p,
                    (p1, p2) -> {
                        Integer l1 = p1.getAccessLevel();
                        Integer l2 = p2.getAccessLevel();
                        return (l1 != null ? l1 : 0) >= (l2 != null ? l2 : 0) ? p1 : p2;
                    }
                ))
                .values().stream()
                .map(Permission::getId)
                .collect(Collectors.toList());

            for (UUID pId : filteredIds) {
                assignPermissionToRole(roleId, pId);
            }
        }

        return getRoleDetails(roleId);
    }

    @Override
    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        role.setIsActive(false);
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void recoverRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        role.setIsActive(true);
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteResources(List<UUID> roleIds) {
        for (UUID id : roleIds) {
            deleteRole(id);
        }
    }

    @Override
    @Transactional
    public void recoverResources(List<UUID> roleIds) {
        for (UUID id : roleIds) {
            recoverRole(id);
        }
    }

    @Override
    public RoleResponse getRoleDetails(UUID roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        List<RolePermission> mappings = rolePermissionRepository.findByRoleId(roleId);
        
        List<PermissionInfo> perms = mappings.stream()
                .collect(Collectors.toMap(
                    rp -> rp.getPermission().getName(),
                    rp -> PermissionInfo.builder()
                        .id(rp.getPermission().getId())
                        .name(rp.getPermission().getName())
                        .accessLevel(rp.getPermission().getAccessLevel())
                        .build(),
                    (p1, p2) -> {
                        Integer l1 = p1.getAccessLevel();
                        Integer l2 = p2.getAccessLevel();
                        return (l1 != null ? l1 : 0) >= (l2 != null ? l2 : 0) ? p1 : p2;
                    }
                ))
                .values().stream()
                .collect(Collectors.toList());

        RoleResponse response = mapToRoleResponse(role);
        response.setPermissions(perms);
        return response;
    }

    @Override
    public PageResponse<List<RoleResponse>> getAllRoles(Pageable pageable) {
        Page<Role> rolePage = roleRepository.findAll(pageable);
        return PageResponse.fromPage(rolePage, role -> getRoleDetails(role.getId()));
    }

    // --- PERMISSIONS ---

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        Permission permission = Permission.builder()
                .name(request.getName())
                .accessLevel(request.getAccessLevel())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return mapToPermissionResponse(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(UUID permissionId, PermissionRequest request) {
        Permission p = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        
        p.setName(request.getName());
        p.setAccessLevel(request.getAccessLevel());
        if (request.getIsActive() != null) {
            p.setIsActive(request.getIsActive());
        }

        return mapToPermissionResponse(permissionRepository.save(p));
    }

    @Override
    @Transactional
    public void deletePermission(UUID permissionId) {
        Permission p = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        p.setIsActive(false);
        permissionRepository.save(p);
    }

    @Override
    @Transactional
    public void recoverPermission(UUID permissionId) {
        Permission p = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        p.setIsActive(true);
        permissionRepository.save(p);
    }

    @Override
    @Transactional
    public void deletePermissionResources(List<UUID> permissionIds) {
        for (UUID id : permissionIds) {
            deletePermission(id);
        }
    }

    @Override
    @Transactional
    public void recoverPermissionResources(List<UUID> permissionIds) {
        for (UUID id : permissionIds) {
            recoverPermission(id);
        }
    }

    @Override
    public PageResponse<List<PermissionResponse>> getAllPermissions(Pageable pageable) {
        Page<Permission> permissionPage = permissionRepository.findAll(pageable);
        return PageResponse.fromPage(permissionPage, this::mapToPermissionResponse);
    }

    // --- ASSIGNMENTS ---

    @Override
    @Transactional
    public void assignPermissionToRole(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        Permission perm = permissionRepository.findById(permissionId).orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));

        RolePermission rp = RolePermission.builder()
                .role(role)
                .permission(perm)
                .build();
        rolePermissionRepository.save(rp);
    }

    // --- HELPERS ---

    private RoleResponse mapToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .isSystemRole(role.getIsSystemRole())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .createdBy(role.getCreatedBy())
                .updatedAt(role.getUpdatedAt())
                .updatedBy(role.getUpdatedBy())
                .isActive(role.getIsActive())
                .build();
    }

    private PermissionResponse mapToPermissionResponse(Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .accessLevel(p.getAccessLevel())
                .createdAt(p.getCreatedAt())
                .createdBy(p.getCreatedBy())
                .updatedAt(p.getUpdatedAt())
                .updatedBy(p.getUpdatedBy())
                .isActive(p.getIsActive())
                .build();
    }
}
