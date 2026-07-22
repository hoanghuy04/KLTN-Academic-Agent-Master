package com.bondhub.Assistant.dto.response;

import com.bondhub.Assistant.entity.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@Data @Builder
public class UserDetailResponse {
    private UUID id;
    private String email;
    private String personalEmail;
    private String firstName;
    private String lastName;
    private String avtUrl;
    private String code;
    private String roleName;
    private UUID roleId;
    private UUID accountId;
    private String phone;
    private String gender;
    private UserStatus status;
    private LocalDateTime lastLogin;
    
    // Base Entity fields
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    // Additional fields
    private String address;
    private String department;
    
    @Builder.Default
    private Integer totalQueries = 0;
    
    private List<String> topTopics;
    
    private Map<String, Integer> permissions;
}
