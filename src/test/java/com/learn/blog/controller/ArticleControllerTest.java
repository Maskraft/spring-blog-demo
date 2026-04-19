package com.learn.blog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.blog.dto.ArticleRequest;
import com.learn.blog.dto.ArticleResponse;
import com.learn.blog.exception.ArticleNotFoundException;
import com.learn.blog.security.CustomUserDetailsService;
import com.learn.blog.security.SecurityConfig;
import com.learn.blog.service.ArticleService;

// ArticleController の HTTP 層テスト。Service をモック化し、ルーティング・バリデーション・例外処理を検証する。
// SecurityConfig を @Import することで @PreAuthorize と認可ルールを実環境と同じ設定で動かす
@WebMvcTest(ArticleController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ArticleControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ArticleService articleService;

    // SecurityConfig が UserDetailsService Bean を要求するためモック化
    @MockitoBean private CustomUserDetailsService userDetailsService;

    private ArticleResponse sampleResponse(Long id) {
        return new ArticleResponse(
                id, "タイトル" + id, "本文" + id, LocalDateTime.of(2026, 4, 17, 10, 0));
    }

    @Test
    @DisplayName("GET /api/v1/articles: 一覧を 200 OK で返す")
    void list_returnsOk() throws Exception {
        when(articleService.findAll()).thenReturn(List.of(sampleResponse(1L), sampleResponse(2L)));

        mockMvc.perform(get("/api/v1/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("タイトル1"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/articles/{id}: 存在する場合 200 OK")
    void getOne_returnsOk() throws Exception {
        when(articleService.findById(1L)).thenReturn(sampleResponse(1L));

        mockMvc.perform(get("/api/v1/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("タイトル1"))
                .andExpect(jsonPath("$.content").value("本文1"));
    }

    @Test
    @DisplayName("GET /api/v1/articles/{id}: 存在しない場合 404 と統一エラーレスポンス")
    void getOne_returnsNotFound() throws Exception {
        when(articleService.findById(99L)).thenThrow(new ArticleNotFoundException(99L));

        mockMvc.perform(get("/api/v1/articles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("id=99 の記事が見つかりません"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /api/v1/articles: 正常な入力で 201 Created と Location ヘッダを返す")
    @WithMockUser(roles = "ADMIN")
    void create_returnsCreated() throws Exception {
        ArticleRequest request = new ArticleRequest("新タイトル", "新本文");
        when(articleService.create(any(ArticleRequest.class))).thenReturn(sampleResponse(10L));

        mockMvc.perform(
                        post("/api/v1/articles")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/articles/10"))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("POST /api/v1/articles: 未認証の場合 401")
    void create_returnsUnauthorizedWhenAnonymous() throws Exception {
        String json = "{\"title\":\"タイトル\",\"content\":\"本文\"}";

        mockMvc.perform(
                        post("/api/v1/articles")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isUnauthorized());
        verify(articleService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/v1/articles: USER ロールでは 403")
    @WithMockUser(roles = "USER")
    void create_returnsForbiddenWhenNotAdmin() throws Exception {
        String json = "{\"title\":\"タイトル\",\"content\":\"本文\"}";

        mockMvc.perform(
                        post("/api/v1/articles")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isForbidden());
        verify(articleService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/v1/articles: title が空の場合 400 とバリデーションエラー")
    @WithMockUser(roles = "ADMIN")
    void create_returnsBadRequestWhenTitleBlank() throws Exception {
        String json = "{\"title\":\"\",\"content\":\"本文\"}";

        mockMvc.perform(
                        post("/api/v1/articles")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.message").value(org.hamcrest.Matchers.containsString("title")));
        verify(articleService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/v1/articles: content が空の場合 400")
    @WithMockUser(roles = "ADMIN")
    void create_returnsBadRequestWhenContentBlank() throws Exception {
        String json = "{\"title\":\"タイトル\",\"content\":\"\"}";

        mockMvc.perform(
                        post("/api/v1/articles")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(org.hamcrest.Matchers.containsString("content")));
    }

    @Test
    @DisplayName("POST /api/v1/articles: title が 201 文字の場合 400（@Size(max=200) に違反）")
    @WithMockUser(roles = "ADMIN")
    void create_returnsBadRequestWhenTitleTooLong() throws Exception {
        String longTitle = "あ".repeat(201);
        String json = "{\"title\":\"" + longTitle + "\",\"content\":\"本文\"}";

        mockMvc.perform(
                        post("/api/v1/articles")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message").value(org.hamcrest.Matchers.containsString("200")));
    }

    @Test
    @DisplayName("PUT /api/v1/articles/{id}: 正常な更新で 200 OK")
    @WithMockUser(roles = "ADMIN")
    void update_returnsOk() throws Exception {
        ArticleRequest request = new ArticleRequest("更新後", "更新本文");
        when(articleService.update(eq(1L), any(ArticleRequest.class)))
                .thenReturn(sampleResponse(1L));

        mockMvc.perform(
                        put("/api/v1/articles/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/articles/{id}: 対象が存在しない場合 404")
    @WithMockUser(roles = "ADMIN")
    void update_returnsNotFound() throws Exception {
        ArticleRequest request = new ArticleRequest("更新後", "更新本文");
        when(articleService.update(eq(99L), any(ArticleRequest.class)))
                .thenThrow(new ArticleNotFoundException(99L));

        mockMvc.perform(
                        put("/api/v1/articles/99")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/v1/articles/{id}: 正常削除で 204 No Content")
    @WithMockUser(roles = "ADMIN")
    void delete_returnsNoContent() throws Exception {
        doNothing().when(articleService).delete(1L);

        mockMvc.perform(delete("/api/v1/articles/1").with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(articleService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/articles/{id}: 対象が存在しない場合 404")
    @WithMockUser(roles = "ADMIN")
    void delete_returnsNotFound() throws Exception {
        doThrow(new ArticleNotFoundException(99L)).when(articleService).delete(99L);

        mockMvc.perform(delete("/api/v1/articles/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
