package com.bondhub.Assistant.controller;

import com.bondhub.Assistant.dto.request.LoginRequest;
import com.bondhub.Assistant.dto.request.SelectProfileRequest;
import com.bondhub.Assistant.dto.response.ApiResponse;
import com.bondhub.Assistant.dto.response.AuthResponse;
import com.bondhub.Assistant.dto.response.SelectProfileResponse;
import com.bondhub.Assistant.service.AuthService;
import com.bondhub.Assistant.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

        private final AuthService authService;
        private final CookieUtil cookieUtil;

        @PostMapping("/login")
        public ResponseEntity<ApiResponse<AuthResponse>> login(
                        @Valid @RequestBody LoginRequest request,
                        HttpServletResponse httpResponse) {

                log.info("POST /api/auth/login - code: {}", request.getCode());

                AuthResponse response = authService.login(request);

                // If tokens are present (single profile auto-login), issue cookies
                if (response.getAccessToken() != null) {
                        ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(
                                        response.getAccessToken(), 86400000L);
                        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

                        ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(
                                        response.getRefreshToken(), response.getRefreshTokenExpirationMs());
                        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
                }

                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @PostMapping("/select-profile")
        public ResponseEntity<ApiResponse<SelectProfileResponse>> selectProfile(
                        @Valid @RequestBody SelectProfileRequest request,
                        HttpServletResponse httpResponse) {

                log.info("POST /api/auth/select-profile - userId: {}", request.getUserId());

                SelectProfileResponse response = authService.selectProfile(request);

                ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(
                                response.getAccessToken(), 86400000L);
                httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

                ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(
                                response.getRefreshToken(), response.getRefreshTokenExpirationMs());
                httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<AuthResponse>> refresh(
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse) {

                log.info("POST /api/auth/refresh");

                String refreshToken = cookieUtil.extractRefreshTokenFromCookie(httpRequest);

                AuthResponse response = authService.refresh(refreshToken);

                ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(
                                response.getAccessToken(), 86400000L);
                httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

                ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(
                                response.getRefreshToken(), response.getRefreshTokenExpirationMs());
                httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse httpResponse) {
                log.info("POST /api/auth/logout");

                ResponseCookie accessCookie = cookieUtil.clearAccessTokenCookie();
                httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

                ResponseCookie refreshCookie = cookieUtil.clearRefreshTokenCookie();
                httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

                return ResponseEntity.ok(ApiResponse.success(null));
        }
}