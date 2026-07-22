package com.bondhub.Assistant.service;

import com.bondhub.Assistant.dto.request.LoginRequest;
import com.bondhub.Assistant.dto.request.SelectProfileRequest;
import com.bondhub.Assistant.dto.response.AuthResponse;
import com.bondhub.Assistant.dto.response.SelectProfileResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    SelectProfileResponse selectProfile(SelectProfileRequest request);

    AuthResponse refresh(String refreshToken);
}