package com.learn.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ユーザー登録リクエスト。ロールはサーバー側で USER に固定するためフィールドに含めない
public record RegisterRequest(
        @NotBlank(message = "username は必須です")
                @Size(min = 3, max = 50, message = "username は 3 〜 50 文字で入力してください")
                String username,
        @NotBlank(message = "password は必須です")
                @Size(min = 8, max = 72, message = "password は 8 〜 72 文字で入力してください")
                String password) {}
