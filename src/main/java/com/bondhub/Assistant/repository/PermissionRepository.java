package com.bondhub.Assistant.repository;

import com.bondhub.Assistant.entity.Permission;
import com.bondhub.Assistant.entity.enums.PermissionMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndAccessLevel(String name, Integer accessLevel);

    Optional<Permission> findByNameAndAccessLevel(String name, Integer accessLevel);

    boolean existsByPathAndMethod(String path, PermissionMethod method);

    @Query("SELECT p FROM Permission p WHERE p.path = :path AND p.method = :method")
    Optional<Permission> findByPathAndMethod(@Param("path") String path,
                                             @Param("method") PermissionMethod method);

    @Query("SELECT p FROM Permission p WHERE p.resourceType = :resourceType")
    List<Permission> findByResourceType(@Param("resourceType")
                                        com.bondhub.Assistant.entity.enums.ResourceType resourceType);
}