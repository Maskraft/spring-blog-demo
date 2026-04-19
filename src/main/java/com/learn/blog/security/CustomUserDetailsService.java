package com.learn.blog.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.learn.blog.repository.UserRepository;

// Spring Security の認証フローで呼ばれ、DB から UserDetails を取得する。
// ビジネスサービスではなく Spring Security の SPI 実装のため、@Service ではなく @Component を使う
// (ArchitectureTest: @Service は service パッケージ専用のルールがあるため)
@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .map(
                        u ->
                                User.withUsername(u.getUsername())
                                        .password(u.getPassword())
                                        .authorities(
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_" + u.getRole().name())))
                                        .build())
                .orElseThrow(
                        () ->
                                new UsernameNotFoundException(
                                        "username=" + username + " のユーザーが見つかりません"));
    }
}
