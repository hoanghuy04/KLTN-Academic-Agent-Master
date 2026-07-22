package com.bondhub.Assistant.service.Impl;

import com.bondhub.Assistant.dto.request.LoginRequest;
import com.bondhub.Assistant.dto.request.SelectProfileRequest;
import com.bondhub.Assistant.dto.response.AuthResponse;
import com.bondhub.Assistant.dto.response.PermissionInfo;
import com.bondhub.Assistant.dto.response.SelectProfileResponse;
import com.bondhub.Assistant.dto.response.UserProfileResponse;
import com.bondhub.Assistant.entity.Account;
import com.bondhub.Assistant.entity.Permission;
import com.bondhub.Assistant.entity.User;
import com.bondhub.Assistant.exception.AppException;
import com.bondhub.Assistant.exception.ErrorCode;
import com.bondhub.Assistant.repository.UserRepository;
import com.bondhub.Assistant.repository.AccountRepository;
import com.bondhub.Assistant.security.JwtUtil;
import com.bondhub.Assistant.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Account account = accountRepository.findByCode(request.getCode())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!account.getIsActive()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new AppException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        List<User> users = account.getUsers();
        if (users.isEmpty()) {
            throw new AppException(ErrorCode.PROFILE_NOT_FOUND);
        }

        // Update last login
        account.setLastLogin(LocalDateTime.now());
        accountRepository.save(account);

        // Build profiles list
        List<UserProfileResponse> profiles = users.stream()
                .filter(u -> u.getRole().getIsActive())
                .map(u -> UserProfileResponse.builder()
                        .userId(u.getId())
                        .fullName(u.getFirstName() + " " + u.getLastName())
                        .avatarUrl(u.getAvatarUrl())
                        .role(u.getRole().getName())
                        .roleDescription(u.getRole().getDescription())
                        .isSystemRole(Boolean.TRUE.equals(u.getRole().getIsSystemRole()))
                        .permissions(mapToPermissionInfo(u))
                        .build())
                .collect(Collectors.toList());

        AuthResponse response = AuthResponse.builder()
                .profiles(profiles)
                .email(account.getEmail())
                .code(account.getCode())
                .build();

        // If only one profile, auto-issue tokens
        if (users.size() == 1) {
            User user = users.get(0);
            if(!user.getRole().getIsActive()) {
                throw new AppException(ErrorCode.USER_BANNED);
            }
            AuthResponse session = buildSession(user, account);
            response.setAccessToken(session.getAccessToken());
            response.setRefreshToken(session.getRefreshToken());
            response.setRefreshTokenExpirationMs(session.getRefreshTokenExpirationMs());
        }

        return response;
    }

    @Override
    @Transactional
    public SelectProfileResponse selectProfile(SelectProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        if(!user.getRole().getIsActive()) {
            throw new AppException(ErrorCode.USER_BANNED);
        }
        Account account = user.getAccount();

        if (!account.getIsActive()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return SelectProfileResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenExpirationMs(jwtUtil.getRefreshExpirationMs())
                .email(account.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole().getName())
                .code(account.getCode())
                .avatarUrl(user.getAvatarUrl())
                .isSystemRole(Boolean.TRUE.equals(user.getRole().getIsSystemRole()))
                .permissions(mapToPermissionInfo(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || !jwtUtil.validate(refreshToken)) {
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }

        String userIdStr = jwtUtil.getUserId(refreshToken);
        if (userIdStr == null) {
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }

        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Account account = user.getAccount();

        if (!account.getIsActive()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        return buildSession(user, account);
    }

    private AuthResponse buildSession(User user, Account account) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(account.getEmail())
                .code(account.getCode())
                .refreshTokenExpirationMs(jwtUtil.getRefreshExpirationMs())
                .build();
    }

    private List<PermissionInfo> mapToPermissionInfo(User user) {
        return user.getRole().getRolePermissions().stream()
                .map(rp -> {
                    Permission p = rp.getPermission();
                    return PermissionInfo.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .accessLevel(p.getAccessLevel())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
