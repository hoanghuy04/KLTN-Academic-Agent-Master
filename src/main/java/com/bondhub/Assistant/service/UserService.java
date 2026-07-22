package com.bondhub.Assistant.service;
import com.bondhub.Assistant.dto.request.CreateUserRequest;
import com.bondhub.Assistant.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import com.bondhub.Assistant.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

import com.bondhub.Assistant.dto.request.UpdateUserRequest;
import com.bondhub.Assistant.dto.response.UserDetailResponse;

public interface UserService {
 UserResponse createUser(CreateUserRequest request);
 UserDetailResponse getUserById(UUID id);
 PageResponse<List<UserResponse>> getAllUsers(Pageable pageable);
 UserDetailResponse updateUser(UUID id, UpdateUserRequest request);
 void deleteUser(UUID id);
 void recoverUser(UUID id);
 void deleteResources(List<UUID> ids);
 void recoverResources(List<UUID> ids);
}
