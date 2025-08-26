package com.example.wiseai_dev.user.application.api.controller;

import com.example.wiseai_dev.global.ApiResponse;
import com.example.wiseai_dev.user.application.api.dto.UserRequest;
import com.example.wiseai_dev.user.application.api.dto.UserResponse;
import com.example.wiseai_dev.user.domain.model.User;
import com.example.wiseai_dev.user.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @Operation(summary = "사용자 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(saved)));
    }

    @Operation(summary = "사용자 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(user)));
    }
}
