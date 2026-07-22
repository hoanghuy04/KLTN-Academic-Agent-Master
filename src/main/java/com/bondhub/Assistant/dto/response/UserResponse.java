package com.bondhub.Assistant.dto.response;
import com.bondhub.Assistant.entity.enums.UserStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class UserResponse {
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
 private LocalDateTime createdAt;
}
