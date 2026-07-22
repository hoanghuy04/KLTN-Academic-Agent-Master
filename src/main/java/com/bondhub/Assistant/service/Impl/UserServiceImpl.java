package com.bondhub.Assistant.service.Impl;
import com.bondhub.Assistant.dto.request.CreateUserRequest;
import com.bondhub.Assistant.entity.enums.UserStatus;
import com.bondhub.Assistant.dto.request.UpdateUserRequest;
import com.bondhub.Assistant.dto.response.PageResponse;
import com.bondhub.Assistant.dto.response.UserResponse;
import com.bondhub.Assistant.dto.response.UserDetailResponse;

import java.util.*;

import com.bondhub.Assistant.entity.Account;
import com.bondhub.Assistant.entity.Permission;
import com.bondhub.Assistant.entity.Role;
import com.bondhub.Assistant.entity.User;
import com.bondhub.Assistant.repository.PermissionRepository;
import com.bondhub.Assistant.repository.UserRepository;
import com.bondhub.Assistant.repository.RoleRepository;
import com.bondhub.Assistant.repository.AccountRepository;
import com.bondhub.Assistant.service.UserService;
import com.bondhub.Assistant.exception.AppException;
import com.bondhub.Assistant.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

 private final AccountRepository accountRepository;
 private final UserRepository userRepository;
 private final RoleRepository roleRepository;
 private final PermissionRepository permissionRepository;
 private final PasswordEncoder passwordEncoder;

 @Override
 @Transactional
 public UserResponse createUser(CreateUserRequest request) {
     if (request.getPhone() != null && userRepository.findByPhone(request.getPhone()).isPresent())
         throw new AppException(ErrorCode.PHONE_EXISTED);

     Role role = roleRepository.findById(request.getRoleId())
         .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

     Account account = accountRepository.findById(request.getAccountId())
             .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));

     User user = User.builder()
         .account(account)
         .firstName(request.getFirstName())
         .lastName(request.getLastName())
         .role(role)
         .phone(request.getPhone())
         .gender(request.getGender())
         .personalEmail(request.getPersonalEmail())
         .build();
     userRepository.save(user);

     return toResponse(account, user);
 }

 @Override
 public UserDetailResponse getUserById(UUID id) {
     User user = userRepository.findById(id)
         .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
     return toDetailResponse(user.getAccount(), user);
 }

  @Override
  public PageResponse<List<UserResponse>> getAllUsers(Pageable pageable) {
      Page<User> userPage = userRepository.findAll(pageable);
      return PageResponse.fromPage(userPage, u -> toResponse(u.getAccount(), u));
  }

 @Override
 @Transactional
 public UserDetailResponse updateUser(UUID id, UpdateUserRequest request) {
     User user = userRepository.findById(id)
         .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
     Account account = user.getAccount();

     // Check uniqueness constraints
     if (request.getEmail() != null && !request.getEmail().equals(account.getEmail())) {
         if (accountRepository.findByEmail(request.getEmail()).isPresent())
             throw new AppException(ErrorCode.EMAIL_EXISTED);
         account.setEmail(request.getEmail());
     }
     if (request.getCode() != null && !request.getCode().equals(account.getCode())) {
         if (accountRepository.findByCode(request.getCode()).isPresent())
             throw new AppException(ErrorCode.USER_CODE_EXISTED);
         account.setCode(request.getCode());
     }
     if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
         if (userRepository.findByPhone(request.getPhone()).isPresent())
             throw new AppException(ErrorCode.PHONE_EXISTED);
     }

     user.setFirstName(request.getFirstName());
     user.setLastName(request.getLastName());
     user.setPhone(request.getPhone());
     user.setGender(request.getGender());
     user.setAddress(request.getAddress());
     user.setDepartment(request.getDepartment());
     
     accountRepository.save(account);
     
     if (request.getRoleId() != null && (user.getRole() == null || !user.getRole().getId().equals(request.getRoleId()))) {
         Role role = roleRepository.findById(request.getRoleId())
             .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
         user.setRole(role);
     }

     if (request.getAccountId() != null && !request.getAccountId().equals(account.getId())) {
         Account newAccount = accountRepository.findById(request.getAccountId())
             .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));
         user.setAccount(newAccount);
         account = newAccount;
     }
     
     userRepository.save(user);
     return toDetailResponse(account, user);
 }

  @Override
  @Transactional
  public void deleteUser(UUID id) {
      User user = userRepository.findById(id)
          .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
      user.setStatus(UserStatus.INACTIVE);
      userRepository.save(user);
  }

  @Override
  @Transactional
  public void recoverUser(UUID id) {
      User user = userRepository.findById(id)
          .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
      user.setStatus(UserStatus.ACTIVE);
      userRepository.save(user);
  }

  @Override
  @Transactional
  public void deleteResources(List<UUID> ids) {
      List<User> users = userRepository.findAllById(ids);
      users.forEach(user -> user.setStatus(UserStatus.INACTIVE));
      userRepository.saveAll(users);
  }

  @Override
  @Transactional
  public void recoverResources(List<UUID> ids) {
      List<User> users = userRepository.findAllById(ids);
      users.forEach(user -> user.setStatus(UserStatus.ACTIVE));
      userRepository.saveAll(users);
  }

 private UserResponse toResponse(Account account, User user) {
     return UserResponse.builder()
         .id(user != null ? user.getId() : account.getId())
         .accountId(account.getId())
         .email(account.getEmail())
         .personalEmail(user != null ? user.getPersonalEmail() : null)
         .firstName(user != null ? user.getFirstName() : null)
         .lastName(user != null ? user.getLastName() : null)
         .avtUrl(user !=null ? user.getAvatarUrl() : null)
         .code(account.getCode())
         .roleName(user != null && user.getRole() != null ? user.getRole().getName() : null)
         .roleId(user != null && user.getRole() != null ? user.getRole().getId() : null)
         .phone(user != null ? user.getPhone() : null)
         .gender(user != null ? user.getGender() : null)
         .status(user != null ? user.getStatus() : (account.getIsActive() ? UserStatus.ACTIVE : UserStatus.INACTIVE))
         .lastLogin(account.getLastLogin())
         .createdAt(user != null ? user.getCreatedAt() : account.getCreatedAt())
         .build();
 }

 private UserDetailResponse toDetailResponse(Account account, User user) {
     Map<String, Integer> permissionsMap = new HashMap<>();
     if (user != null && user.getRole() != null && user.getRole().getRolePermissions() != null) {
         user.getRole().getRolePermissions().forEach(rp -> {
                Permission p = rp.getPermission();
                Integer level = p.getAccessLevel();
                if (level == null) {
                    permissionsMap.putIfAbsent(p.getName(), null);
                } else {
                    permissionsMap.merge(p.getName(), level, (oldVal, newVal) -> 
                        oldVal == null ? newVal : Math.max(oldVal, newVal));
                }
         });
     }

     return UserDetailResponse.builder()
         .id(user != null ? user.getId() : account.getId())
         .accountId(account.getId())
         .email(account.getEmail())
         .personalEmail(user != null ? user.getPersonalEmail() : null)
         .firstName(user != null ? user.getFirstName() : null)
         .lastName(user != null ? user.getLastName() : null)
         .avtUrl(user != null ? user.getAvatarUrl() : null)
         .code(account.getCode())
         .roleName(user != null && user.getRole() != null ? user.getRole().getName() : null)
         .roleId(user != null && user.getRole() != null ? user.getRole().getId() : null)
         .phone(user != null ? user.getPhone() : null)
         .gender(user != null ? user.getGender() : null)
         .status(user != null ? user.getStatus() : (account.getIsActive() ? UserStatus.ACTIVE : UserStatus.INACTIVE))
         .lastLogin(account.getLastLogin())
         .createdAt(user != null ? user.getCreatedAt() : account.getCreatedAt())
         .createdBy(user != null ? user.getCreatedBy() : account.getCreatedBy())
         .updatedAt(user != null ? user.getUpdatedAt() : account.getUpdatedAt())
         .updatedBy(user != null ? user.getUpdatedBy() : account.getUpdatedBy())
         .address(user != null ? user.getAddress() : null)
         .department(user != null ? user.getDepartment() : null)
         .totalQueries(0)
         .topTopics(new ArrayList<>())
         .permissions(permissionsMap)
         .build();
 }
}