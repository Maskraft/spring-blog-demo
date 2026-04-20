package com.learn.blog.controller;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learn.blog.dto.ArticleRequest;
import com.learn.blog.dto.ArticleResponse;
import com.learn.blog.service.ArticleService;

// 記事の REST API。共通プレフィックスは /api/v1/articles（API バージョニング）
@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<ArticleResponse> list() {
        return articleService.findAll();
    }

    @GetMapping("/{id}")
    public ArticleResponse getOne(@PathVariable Long id) {
        return articleService.findById(id);
    }

    // 認証済みユーザーなら誰でも記事を作成できる。SecurityConfig の anyRequest().authenticated() で担保
    @PostMapping
    public ResponseEntity<ArticleResponse> create(
            @Valid @RequestBody ArticleRequest request, Principal principal) {
        ArticleResponse created = articleService.create(request, principal.getName());
        return ResponseEntity.created(URI.create("/api/v1/articles/" + created.id())).body(created);
    }

    // 所有者チェックは ArticleService 内で行う（ADMIN は全記事編集可）
    @PutMapping("/{id}")
    public ArticleResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest request,
            Principal principal) {
        return articleService.update(id, request, principal.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        articleService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
