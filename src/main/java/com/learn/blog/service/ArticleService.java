package com.learn.blog.service;

import com.learn.blog.dto.ArticleRequest;
import com.learn.blog.entity.Article;
import com.learn.blog.exception.ArticleNotFoundException;
import com.learn.blog.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// ビジネスサービス層：記事の CRUD 操作をカプセル化
@Service
@Transactional
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    public Article create(ArticleRequest request) {
        Article article = new Article(request.title(), request.content());
        return articleRepository.save(article);
    }

    public Article update(Long id, ArticleRequest request) {
        Article article = findById(id);
        article.setTitle(request.title());
        article.setContent(request.content());
        return articleRepository.save(article);
    }

    public void delete(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ArticleNotFoundException(id);
        }
        articleRepository.deleteById(id);
    }
}
