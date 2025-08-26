package com.example.wiseai_dev.user.application.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "사용자 이메일", example = "hong@test.com")
    private String email;
}
