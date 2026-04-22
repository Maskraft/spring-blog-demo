package com.learn.blog.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.blog.controller.ArticleController;
import com.learn.blog.dto.ArticleRequest;
import com.learn.blog.dto.ArticleResponse;
import com.learn.blog.exception.ArticleNotFoundException;
import com.learn.blog.security.CustomUserDetailsService;
import com.learn.blog.security.SecurityConfig;
import com.learn.blog.service.ArticleService;

// Spring REST Docs 用のドキュメント生成テスト。
// 各エンドポイントに対して document(...) を呼び、リクエスト/レスポンスのスニペットを生成する。
// 生成先: target/generated-snippets/<identifier>/*.adoc
@WebMvcTest(ArticleController.class)
@Import(SecurityConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = "ADMIN")
class ArticleApiDocumentation {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ArticleService articleService;

    @MockitoBean private CustomUserDetailsService userDetailsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        // REST Docs の拡張を MockMvc に組み込み、各リクエストで document() を使えるようにする
        this.mockMvc =
                webAppContextSetup(webApplicationContext)
                        .apply(documentationConfiguration(restDocumentation))
                        .apply(springSecurity())
                        .build();
    }

    private ArticleResponse sampleResponse(Long id) {
        return new ArticleResponse(
                id, "サンプルタイトル", "サンプル本文", LocalDateTime.of(2026, 4, 17, 10, 0), "admin");
    }

    @Test
    @DisplayName("GET /api/v1/articles: 一覧取得のドキュメント")
    void listArticles() throws Exception {
        when(articleService.findAll()).thenReturn(List.of(sampleResponse(1L), sampleResponse(2L)));

        mockMvc.perform(get("/api/v1/articles"))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "articles-list",
                                responseFields(
                                        fieldWithPath("[].id").description("記事 ID"),
                                        fieldWithPath("[].title").description("記事タイトル"),
                                        fieldWithPath("[].content").description("記事本文"),
                                        fieldWithPath("[].createdAt").description("作成日時（ISO-8601）"),
                                        fieldWithPath("[].authorUsername")
                                                .description("投稿者のユーザー名"))));
    }

    @Test
    @DisplayName("GET /api/v1/articles/{id}: 単件取得のドキュメント")
    void getArticle() throws Exception {
        when(articleService.findById(1L)).thenReturn(sampleResponse(1L));

        mockMvc.perform(get("/api/v1/articles/{id}", 1L))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "articles-get",
                                pathParameters(parameterWithName("id").description("記事 ID")),
                                responseFields(
                                        fieldWithPath("id").description("記事 ID"),
                                        fieldWithPath("title").description("記事タイトル"),
                                        fieldWithPath("content").description("記事本文"),
                                        fieldWithPath("createdAt").description("作成日時（ISO-8601）"),
                                        fieldWithPath("authorUsername").description("投稿者のユーザー名"))));
    }

    @Test
    @DisplayName("POST /api/v1/articles: 作成のドキュメント")
    void createArticle() throws Exception {
        ArticleRequest request = new ArticleRequest("新規タイトル", "新規本文");
        when(articleService.create(any(ArticleRequest.class), eq("admin")))
                .thenReturn(sampleResponse(10L));

        mockMvc.perform(
                        post("/api/v1/articles")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(
                        document(
                                "articles-create",
                                requestFields(
                                        fieldWithPath("title").description("記事タイトル（必須・200 文字以内）"),
                                        fieldWithPath("content").description("記事本文（必須）")),
                                responseHeaders(
                                        headerWithName("Location").description("作成された記事の URI")),
                                responseFields(
                                        fieldWithPath("id").description("記事 ID"),
                                        fieldWithPath("title").description("記事タイトル"),
                                        fieldWithPath("content").description("記事本文"),
                                        fieldWithPath("createdAt").description("作成日時（ISO-8601）"),
                                        fieldWithPath("authorUsername").description("投稿者のユーザー名"))));
    }

    @Test
    @DisplayName("PUT /api/v1/articles/{id}: 更新のドキュメント")
    void updateArticle() throws Exception {
        ArticleRequest request = new ArticleRequest("更新後タイトル", "更新後本文");
        when(articleService.update(eq(1L), any(ArticleRequest.class), any(Authentication.class)))
                .thenReturn(sampleResponse(1L));

        mockMvc.perform(
                        put("/api/v1/articles/{id}", 1L)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "articles-update",
                                pathParameters(parameterWithName("id").description("記事 ID")),
                                requestFields(
                                        fieldWithPath("title").description("記事タイトル（必須・200 文字以内）"),
                                        fieldWithPath("content").description("記事本文（必須）")),
                                responseFields(
                                        fieldWithPath("id").description("記事 ID"),
                                        fieldWithPath("title").description("記事タイトル"),
                                        fieldWithPath("content").description("記事本文"),
                                        fieldWithPath("createdAt").description("作成日時（ISO-8601）"),
                                        fieldWithPath("authorUsername").description("投稿者のユーザー名"))));
    }

    @Test
    @DisplayName("DELETE /api/v1/articles/{id}: 削除のドキュメント")
    void deleteArticle() throws Exception {
        doNothing().when(articleService).delete(eq(1L), any(Authentication.class));

        mockMvc.perform(delete("/api/v1/articles/{id}", 1L).with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(
                        document(
                                "articles-delete",
                                pathParameters(parameterWithName("id").description("記事 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/articles/{id}: 404 エラーレスポンスのドキュメント")
    void getArticleNotFound() throws Exception {
        when(articleService.findById(99L)).thenThrow(new ArticleNotFoundException(99L));

        mockMvc.perform(get("/api/v1/articles/{id}", 99L))
                .andExpect(status().isNotFound())
                .andDo(
                        document(
                                "articles-error-not-found",
                                pathParameters(parameterWithName("id").description("記事 ID")),
                                responseFields(
                                        fieldWithPath("timestamp").description("エラー発生日時（ISO-8601）"),
                                        fieldWithPath("status").description("HTTP ステータスコード"),
                                        fieldWithPath("error").description("HTTP ステータス名"),
                                        fieldWithPath("message").description("エラーメッセージ"))));
    }

    @Test
    @DisplayName("POST /api/v1/articles: 400 バリデーションエラーレスポンスのドキュメント")
    void createArticleValidationError() throws Exception {
        String invalidJson = "{\"title\":\"\",\"content\":\"本文\"}";

        mockMvc.perform(
                        post("/api/v1/articles")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andDo(
                        document(
                                "articles-error-validation",
                                responseFields(
                                        fieldWithPath("timestamp").description("エラー発生日時（ISO-8601）"),
                                        fieldWithPath("status").description("HTTP ステータスコード"),
                                        fieldWithPath("error").description("HTTP ステータス名"),
                                        fieldWithPath("message")
                                                .description(
                                                        "バリデーションエラーの詳細（フィールド毎の違反" + "メッセージ）"))));
    }
}
