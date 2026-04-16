package com.learn.blog.exception;

// 記事が見つからない例外。グローバルハンドラにより 404 に変換される
public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(Long id) {
        super("id=" + id + " の記事が見つかりません");
    }
}
