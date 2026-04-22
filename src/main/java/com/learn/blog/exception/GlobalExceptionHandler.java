package com.learn.blog.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// グローバル例外ハンドラ：業務例外とバリデーション例外を統一した JSON エラーレスポンスに変換
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ArticleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildBody(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameConflict(
            UsernameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT, ex.getMessage()));
    }

    // ログイン失敗：username 不存在 / password 不一致を区別せず 401 を返す (アカウント列挙攻撃対策)
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleBadCredentials(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildBody(HttpStatus.UNAUTHORIZED, "username または password が正しくありません"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {
        String message =
                ex.getBindingResult().getFieldErrors().stream()
                        .findFirst()
                        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                        .orElse("パラメータの検証に失敗しました");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST, message));
    }

    private Map<String, Object> buildBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
