package com.learn.blog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.learn.blog.dto.UserResponse;
import com.learn.blog.entity.Role;
import com.learn.blog.exception.UsernameAlreadyExistsException;
import com.learn.blog.security.CustomUserDetailsService;
import com.learn.blog.security.SecurityConfig;
import com.learn.blog.service.AuthService;

// AuthController の HTTP 層テスト。AuthService と AuthenticationManager をモック化し、
// SecurityConfig を @Import することで本番と同じ認可ルール・CSRF・/logout を動作させる
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AuthService authService;

    // SecurityConfig が AuthenticationManager / UserDetailsService Bean を要求するためモック化
    @MockitoBean private AuthenticationManager authenticationManager;

    @MockitoBean private CustomUserDetailsService userDetailsService;

    // ---------- /register ----------

    @Test
    @DisplayName("POST /api/v1/auth/register: 正常な登録で 201 Created と Location ヘッダ")
    void register_returnsCreated() throws Exception {
        when(authService.register(any())).thenReturn(new UserResponse(1L, "alice", Role.USER));

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"alice\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/auth/me"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register: username 重複で 409 Conflict")
    void register_returnsConflictWhenUsernameExists() throws Exception {
        when(authService.register(any())).thenThrow(new UsernameAlreadyExistsException("alice"));

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"alice\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(
                        jsonPath("$.message").value(org.hamcrest.Matchers.containsString("alice")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register: username が短すぎる場合 400")
    void register_returnsBadRequestWhenUsernameTooShort() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"ab\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(org.hamcrest.Matchers.containsString("username")));
        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register: password が短すぎる場合 400")
    void register_returnsBadRequestWhenPasswordTooShort() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"alice\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(org.hamcrest.Matchers.containsString("password")));
        verify(authService, never()).register(any());
    }

    // ---------- /login ----------

    @Test
    @DisplayName("POST /api/v1/auth/login: 正しい資格で 200 OK と UserResponse")
    void login_returnsOk() throws Exception {
        when(authService.login(any(), any(), any()))
                .thenReturn(new UserResponse(1L, "alice", Role.USER));

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"alice\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login: 認証失敗で 401 と統一エラーメッセージ")
    void login_returnsUnauthorizedOnBadCredentials() throws Exception {
        when(authService.login(any(), any(), any())).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"alice\",\"password\":\"wrong123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        org.hamcrest.Matchers.containsString(
                                                "username または password")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login: username 空で 400")
    void login_returnsBadRequestWhenUsernameBlank() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
        verify(authService, never()).login(any(), any(), any());
    }

    // ---------- /me ----------

    @Test
    @DisplayName("GET /api/v1/auth/me: 認証済みユーザーの情報を返す")
    @WithMockUser(username = "alice", roles = "USER")
    void me_returnsUser() throws Exception {
        when(authService.findByUsername("alice"))
                .thenReturn(new UserResponse(1L, "alice", Role.USER));

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me: 未認証で 401")
    void me_returnsUnauthorizedWhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        verify(authService, never()).findByUsername(any());
    }

    // ---------- /logout ----------

    @Test
    @DisplayName("POST /api/v1/auth/logout: 認証済みなら 204 No Content")
    @WithMockUser(username = "alice", roles = "USER")
    void logout_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout").with(csrf())).andExpect(status().isNoContent());
    }
}
