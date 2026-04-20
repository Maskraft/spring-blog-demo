package com.learn.blog.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learn.blog.dto.ArticleRequest;
import com.learn.blog.dto.ArticleResponse;
import com.learn.blog.entity.Article;
import com.learn.blog.entity.Role;
import com.learn.blog.entity.User;
import com.learn.blog.exception.ArticleNotFoundException;
import com.learn.blog.repository.ArticleRepository;
import com.learn.blog.repository.UserRepository;

// ビジネスサービス層：記事の CRUD 操作をカプセル化
@Service
@Transactional
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public ArticleService(ArticleRepository articleRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> findAll() {
        return articleRepository.findAll().stream().map(ArticleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ArticleResponse findById(Long id) {
        return ArticleResponse.from(getArticle(id));
    }

    public ArticleResponse create(ArticleRequest request, String username) {
        User author =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません: " + username));
        Article article = new Article(request.title(), request.content(), author);
        return ArticleResponse.from(articleRepository.save(article));
    }

    public ArticleResponse update(Long id, ArticleRequest request, String username) {
        Article article = getArticle(id);
        checkOwnership(article, username);
        article.setTitle(request.title());
        article.setContent(request.content());
        return ArticleResponse.from(articleRepository.save(article));
    }

    public void delete(Long id, String username) {
        Article article = getArticle(id);
        checkOwnership(article, username);
        articleRepository.deleteById(id);
    }

    // 記事の所有者またはADMINかチェックする。違反時は AccessDeniedException をスロー
    private void checkOwnership(Article article, String username) {
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません: " + username));
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (!article.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("この操作を行う権限がありません");
        }
    }

    // 内部で使用するエンティティ取得メソッド。存在しない場合は例外を投げる
    private Article getArticle(Long id) {
        return articleRepository.findById(id).orElseThrow(() -> new ArticleNotFoundException(id));
    }
}
