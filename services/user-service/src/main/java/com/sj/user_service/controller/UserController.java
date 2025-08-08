package com.sj.user_service.controller;

import com.sj.user_service.dto.UserResponseDto;
import com.sj.user_service.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String requesterEmail) throws AccessDeniedException {
        if (!"ADMIN".equals(userRole)) {
            throw new AccessDeniedException("Access denied. Admin role required.");
        }
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(userService.getUserByEmail(userEmail));
    }

}