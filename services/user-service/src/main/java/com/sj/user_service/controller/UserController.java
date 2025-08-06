package com.sj.user_service.controller;

import com.sj.user_service.dto.LoginRequestDto;
import com.sj.user_service.dto.LoginResponseDto;
import com.sj.user_service.dto.UserRequestDto;
import com.sj.user_service.dto.UserResponseDto;
import com.sj.user_service.entity.User;
import com.sj.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;
    @Operation(summary = "Register a new user", description = "Accepts email, password and role")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRequestDto dto) {
        UserResponseDto user = userService.registerUser(dto);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        UserResponseDto user = userService.login(request.getEmail(), request.getPassword());
        User userEntity = userService.getUserEntityByEmail(request.getEmail());
        String token = userService.generateJwtForUser(userEntity);
        return ResponseEntity.ok(new LoginResponseDto(user.getId(), user.getEmail(), user.getRole(), token));
    }}