package com.learn.blog.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learn.blog.dto.RegisterRequest;
import com.learn.blog.dto.UserResponse;
import com.learn.blog.entity.Role;
import com.learn.blog.entity.User;
import com.learn.blog.exception.UsernameAlreadyExistsException;
import com.learn.blog.repository.UserRepository;

// 認証関連のビジネスサービス。登録・現在ユーザー取得を提供する。
// ログイン/ログアウト自体は Spring Security のフィルタが直接処理するためここには含めない
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // /register から呼ばれる。常に USER ロールで作成する (ADMIN は昇格させない)
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        User user =
                new User(request.username(), passwordEncoder.encode(request.password()), Role.USER);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .map(UserResponse::from)
                .orElseThrow(() -> new IllegalStateException("認証済みユーザーが DB に存在しません: " + username));
    }
}
