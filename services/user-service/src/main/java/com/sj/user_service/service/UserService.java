package com.sj.user_service.service;

import com.sj.user_service.dto.UserRequestDto;
import com.sj.user_service.dto.UserResponseDto;
import com.sj.user_service.entity.User;

public interface UserService {
    UserResponseDto registerUser(UserRequestDto dto);
    UserResponseDto getUser(Long id);

    UserResponseDto login(String email, String password);
    String generateJwtForUser(User user);

    User getUserEntityByEmail(String email);

    UserResponseDto getUserByEmail(String email);
}