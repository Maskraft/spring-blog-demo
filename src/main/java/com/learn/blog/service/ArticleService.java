package com.learn.blog.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learn.blog.dto.ArticleRequest;
import com.learn.blog.dto.ArticleResponse;
import com.learn.blog.entity.Article;
import com.learn.blog.exception.ArticleNotFoundException;
import com.learn.blog.repository.ArticleRepository;

// ビジネスサービス層：記事の CRUD 操作をカプセル化
@Service
@Transactional
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> findAll() {
        return articleRepository.findAll().stream().map(ArticleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ArticleResponse findById(Long id) {
        return ArticleResponse.from(getArticle(id));
    }

    public ArticleResponse create(ArticleRequest request) {
        Article article = new Article(request.title(), request.content());
        return ArticleResponse.from(articleRepository.save(article));
    }

    public ArticleResponse update(Long id, ArticleRequest request) {
        Article article = getArticle(id);
        article.setTitle(request.title());
        article.setContent(request.content());
        return ArticleResponse.from(articleRepository.save(article));
    }

    public void delete(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ArticleNotFoundException(id);
        }
        articleRepository.deleteById(id);
    }

    // 内部で使用するエンティティ取得メソッド。存在しない場合は例外を投げる
    private Article getArticle(Long id) {
        return articleRepository.findById(id).orElseThrow(() -> new ArticleNotFoundException(id));
    }
}
