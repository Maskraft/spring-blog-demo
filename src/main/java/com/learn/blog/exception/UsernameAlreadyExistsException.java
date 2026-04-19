package com.learn.blog.exception;

// username が既に使用されている場合に投げる。グローバルハンドラで 409 に変換される
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("username=" + username + " は既に使用されています");
    }
}
