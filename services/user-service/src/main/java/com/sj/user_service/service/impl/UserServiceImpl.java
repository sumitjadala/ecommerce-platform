package com.sj.user_service.service.impl;

import com.sj.user_service.dto.UserRequestDto;
import com.sj.user_service.dto.UserResponseDto;
import com.sj.user_service.entity.User;
import com.sj.user_service.repository.UserRepository;
import com.sj.user_service.service.UserService;
import com.sj.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    @Override
    public UserResponseDto registerUser(UserRequestDto dto) {
        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole() == null ? "SELLER" : dto.getRole())
                .username(dto.getUsername())
                .enabled(true)
                .build();
        user = userRepo.save(user);
        return toDto(user);
    }

    @Override
    public UserResponseDto getUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toDto(user);
    }

    @Override
    public UserResponseDto login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new IllegalArgumentException("Invalid credentials");
        return toDto(user);
    }

    @Override
    public String generateJwtForUser(User user) {
        return jwtUtil.generateToken(
                user.getEmail(),
                Map.of("role", user.getRole(), "userId", user.getId(), "sellerId", user.getUuid())
        );
    }

    @Override
    public User getUserEntityByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Email Id"));
        return toDto(user);
    }

    private UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }
}