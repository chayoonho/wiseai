package com.example.wiseai_dev.user.application.api.dto;

import com.example.wiseai_dev.user.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "사용자 이메일", example = "hong@test.com")
    private String email;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
