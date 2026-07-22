package com.bondhub.Assistant.dto.request;

import com.bondhub.Assistant.entity.enums.UserStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {
    private String email;
    private String code;
    private String password;
    private Boolean isActive;
}
