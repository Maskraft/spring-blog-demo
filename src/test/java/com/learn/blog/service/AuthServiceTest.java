package com.learn.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.learn.blog.dto.RegisterRequest;
import com.learn.blog.dto.UserResponse;
import com.learn.blog.entity.Role;
import com.learn.blog.entity.User;
import com.learn.blog.exception.UsernameAlreadyExistsException;
import com.learn.blog.repository.UserRepository;

// AuthService の単体テスト。Repository と PasswordEncoder をモック化し、
// ビジネスルール（重複拒否・USER 固定・パスワードハッシュ化）を検証する
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    @Test
    @DisplayName("register: 新規 username なら BCrypt 経由で USER ロールとして保存し UserResponse を返す")
    void register_savesNewUserWithUserRole() {
        RegisterRequest request = new RegisterRequest("alice", "password123");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASHED");
        when(userRepository.save(any(User.class)))
                .thenAnswer(
                        invocation -> {
                            User u = invocation.getArgument(0);
                            u.setId(1L);
                            return u;
                        });

        UserResponse response = authService.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo(Role.USER);

        // 保存される User オブジェクトの中身を検証：パスワードはハッシュ化済み・ロールは USER 固定
        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getPassword()).isEqualTo("HASHED");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("register: 既存 username なら UsernameAlreadyExistsException を投げ save を呼ばない")
    void register_throwsWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("alice", "password123");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("alice");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findByUsername: 存在する場合 UserResponse を返す")
    void findByUsername_returnsUserResponseWhenExists() {
        User user = new User("alice", "HASHED", Role.ADMIN);
        user.setId(7L);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserResponse response = authService.findByUsername("alice");

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("findByUsername: 不存在の場合 IllegalStateException を投げる（認証済みなのに DB に居ない異常系）")
    void findByUsername_throwsWhenNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.findByUsername("ghost"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ghost");
    }
}
