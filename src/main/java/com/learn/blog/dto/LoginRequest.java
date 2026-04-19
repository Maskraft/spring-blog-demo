package com.learn.blog.dto;

import jakarta.validation.constraints.NotBlank;

// ログインリクエスト。Spring Security の UsernamePasswordAuthenticationToken に渡す
public record LoginRequest(
        @NotBlank(message = "username は必須です") String username,
        @NotBlank(message = "password は必須です") String password) {}
