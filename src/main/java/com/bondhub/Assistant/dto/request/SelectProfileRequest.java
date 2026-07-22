package com.bondhub.Assistant.dto.request;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SelectProfileRequest {
    private UUID userId;
}
