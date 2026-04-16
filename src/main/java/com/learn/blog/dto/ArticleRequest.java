package com.learn.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 記事作成 / 更新用のリクエストボディ。Java 21 の record で簡潔に定義
public record ArticleRequest(
        @NotBlank(message = "title は必須です")
        @Size(max = 200, message = "title は 200 文字以下で入力してください")
        String title,

        @NotBlank(message = "content は必須です")
        String content
) {
}
