package com.bondhub.Assistant.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    @Email
    private String email;
    private UUID roleId;
    private UUID accountId;
    private String code;
    private String phone;
    private String gender;
    private String department;
    private String address;
}
