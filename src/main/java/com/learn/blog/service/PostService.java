package com.learn.blog.service;

import com.learn.blog.dto.PostRequest;
import com.learn.blog.entity.Post;
import com.learn.blog.exception.PostNotFoundException;
import com.learn.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// ビジネスサービス層：記事の CRUD 操作をカプセル化
@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    public Post create(PostRequest request) {
        Post post = new Post(request.title(), request.content());
        return postRepository.save(post);
    }

    public Post update(Long id, PostRequest request) {
        Post post = findById(id);
        post.setTitle(request.title());
        post.setContent(request.content());
        return postRepository.save(post);
    }

    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new PostNotFoundException(id);
        }
        postRepository.deleteById(id);
    }
}
