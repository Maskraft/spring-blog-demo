package com.learn.blog.repository;

import com.learn.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA が基本的な CRUD メソッドを自動生成するため実装不要
public interface PostRepository extends JpaRepository<Post, Long> {
}
