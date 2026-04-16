package com.learn.blog.exception;

// 記事が見つからない例外。グローバルハンドラにより 404 に変換される
public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long id) {
        super("id=" + id + " の記事が見つかりません");
    }
}
