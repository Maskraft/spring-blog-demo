package com.learn.blog.dto;

import java.time.LocalDateTime;

import com.learn.blog.entity.Article;

// 記事レスポンス用 DTO。エンティティを API 層に直接公開しないためのラッパー
public record ArticleResponse(Long id, String title, String content, LocalDateTime createdAt) {

    // エンティティからレスポンス DTO への変換ファクトリ
    public static ArticleResponse from(Article article) {
        return new ArticleResponse(
                article.getId(), article.getTitle(), article.getContent(), article.getCreatedAt());
    }
}
