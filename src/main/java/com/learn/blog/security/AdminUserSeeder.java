package com.learn.blog.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.learn.blog.entity.Role;
import com.learn.blog.entity.User;
import com.learn.blog.repository.UserRepository;

// 起動時に開発用の admin ユーザーを投入する (存在しない場合のみ)。
// test プロファイルではテストごとに @WithMockUser で認証状態を作るため不要。
@Component
@Profile("!test")
public class AdminUserSeeder implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(ADMIN_USERNAME)) {
            return;
        }
        User admin = new User(ADMIN_USERNAME, passwordEncoder.encode(ADMIN_PASSWORD), Role.ADMIN);
        userRepository.save(admin);
    }
}
