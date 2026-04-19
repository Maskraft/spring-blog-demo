package com.learn.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ログインリクエスト。Spring Security の UsernamePasswordAuthenticationToken に渡す。
// 文字数制約は RegisterRequest と同一にしておくことで、明らかに無効な入力は認証処理前に弾く
public record LoginRequest(
        @NotBlank(message = "username は必須です")
                @Size(min = 3, max = 50, message = "username は 3 〜 50 文字で入力してください")
                String username,
        @NotBlank(message = "password は必須です")
                @Size(min = 8, max = 72, message = "password は 8 〜 72 文字で入力してください")
                String password) {}
