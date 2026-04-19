package com.learn.blog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.blog.entity.User;

// ユーザー検索用リポジトリ。Spring Security の認証フローと /register の重複チェックで使用する
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
