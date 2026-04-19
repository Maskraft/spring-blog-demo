package com.learn.blog.dto;

import com.learn.blog.entity.Role;
import com.learn.blog.entity.User;

// ユーザー情報レスポンス。password は絶対に含めない
public record UserResponse(Long id, String username, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
