package com.example.wiseai_dev.user.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
    private Long id;
    private String name;
    private String email;
}
