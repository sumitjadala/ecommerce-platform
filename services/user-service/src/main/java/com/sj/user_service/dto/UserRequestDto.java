package com.sj.user_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRequestDto {
    private String email;
    private String password;
    private String role;
}
