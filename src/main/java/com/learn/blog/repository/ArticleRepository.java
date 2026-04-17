package com.learn.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.blog.entity.Article;

// Spring Data JPA が基本的な CRUD メソッドを自動生成するため実装不要
public interface ArticleRepository extends JpaRepository<Article, Long> {}
