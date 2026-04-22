package com.learn.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.learn.blog.dto.ArticleRequest;
import com.learn.blog.dto.ArticleResponse;
import com.learn.blog.entity.Article;
import com.learn.blog.entity.Role;
import com.learn.blog.entity.User;
import com.learn.blog.exception.ArticleNotFoundException;
import com.learn.blog.repository.ArticleRepository;
import com.learn.blog.repository.UserRepository;

// ArticleService のビジネスロジックを Mockito で検証する単体テスト
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock private ArticleRepository articleRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ArticleService articleService;

    private User newUser(Long id, String username, Role role) {
        User user = new User(username, "hashed", role);
        user.setId(id);
        return user;
    }

    private Authentication auth(String username, String role) {
        return new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    // テスト用の Article を作成（id・createdAt・author を明示的に設定）
    private Article newArticle(Long id, String title, String content, User author) {
        Article article = new Article(title, content, author);
        article.setId(id);
        article.setCreatedAt(LocalDateTime.of(2026, 4, 17, 10, 0));
        return article;
    }

    @Test
    @DisplayName("findAll: 全件を ArticleResponse に変換して返す")
    void findAll_returnsAllArticlesAsResponse() {
        User author = newUser(1L, "user1", Role.USER);
        Article a1 = newArticle(1L, "タイトル1", "本文1", author);
        Article a2 = newArticle(2L, "タイトル2", "本文2", author);
        when(articleRepository.findAll()).thenReturn(List.of(a1, a2));

        List<ArticleResponse> result = articleService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ArticleResponse::id).containsExactly(1L, 2L);
        assertThat(result).extracting(ArticleResponse::title).containsExactly("タイトル1", "タイトル2");
    }

    @Test
    @DisplayName("findAll: 0 件の場合は空リストを返す")
    void findAll_returnsEmptyListWhenNoArticles() {
        when(articleRepository.findAll()).thenReturn(List.of());

        List<ArticleResponse> result = articleService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById: 存在する場合は ArticleResponse を返す")
    void findById_returnsResponseWhenExists() {
        User author = newUser(1L, "user1", Role.USER);
        Article article = newArticle(1L, "タイトル", "本文", author);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        ArticleResponse result = articleService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("タイトル");
        assertThat(result.content()).isEqualTo("本文");
        assertThat(result.createdAt()).isEqualTo(LocalDateTime.of(2026, 4, 17, 10, 0));
        assertThat(result.authorUsername()).isEqualTo("user1");
    }

    @Test
    @DisplayName("findById: 存在しない場合は ArticleNotFoundException を投げる")
    void findById_throwsWhenNotFound() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.findById(99L))
                .isInstanceOf(ArticleNotFoundException.class)
                .hasMessageContaining("id=99");
    }

    @Test
    @DisplayName("create: 保存したエンティティの ArticleResponse を返す")
    void create_savesAndReturnsResponse() {
        User author = newUser(1L, "user1", Role.USER);
        ArticleRequest request = new ArticleRequest("新しいタイトル", "新しい本文");
        Article saved = newArticle(10L, "新しいタイトル", "新しい本文", author);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(author));
        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        ArticleResponse result = articleService.create(request, "user1");

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("新しいタイトル");
        assertThat(result.authorUsername()).isEqualTo("user1");
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    @Test
    @DisplayName("update: 所有者が自分の記事を更新できる")
    void update_modifiesExistingAndReturnsResponse() {
        User author = newUser(1L, "user1", Role.USER);
        Article existing = newArticle(1L, "旧タイトル", "旧本文", author);
        ArticleRequest request = new ArticleRequest("新タイトル", "新本文");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(articleRepository.save(existing)).thenReturn(existing);

        ArticleResponse result = articleService.update(1L, request, auth("user1", "USER"));

        assertThat(result.title()).isEqualTo("新タイトル");
        assertThat(result.content()).isEqualTo("新本文");
        assertThat(existing.getTitle()).isEqualTo("新タイトル");
    }

    @Test
    @DisplayName("update: ADMIN は他ユーザーの記事も更新できる")
    void update_allowsAdminToUpdateOthersArticle() {
        User author = newUser(1L, "user1", Role.USER);
        Article existing = newArticle(1L, "旧タイトル", "旧本文", author);
        ArticleRequest request = new ArticleRequest("新タイトル", "新本文");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(articleRepository.save(existing)).thenReturn(existing);

        ArticleResponse result = articleService.update(1L, request, auth("admin", "ADMIN"));

        assertThat(result.title()).isEqualTo("新タイトル");
    }

    @Test
    @DisplayName("update: 他ユーザーの記事を更新しようとすると AccessDeniedException")
    void update_throwsWhenNotOwner() {
        User author = newUser(1L, "user1", Role.USER);
        Article existing = newArticle(1L, "タイトル", "本文", author);
        ArticleRequest request = new ArticleRequest("新タイトル", "新本文");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> articleService.update(1L, request, auth("other", "USER")))
                .isInstanceOf(AccessDeniedException.class);
        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: 対象が存在しない場合は ArticleNotFoundException を投げ、save は呼ばれない")
    void update_throwsWhenNotFound() {
        ArticleRequest request = new ArticleRequest("タイトル", "本文");
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.update(99L, request, auth("user1", "USER")))
                .isInstanceOf(ArticleNotFoundException.class);
        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete: 所有者が自分の記事を削除できる")
    void delete_removesWhenOwner() {
        User author = newUser(1L, "user1", Role.USER);
        Article article = newArticle(1L, "タイトル", "本文", author);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        articleService.delete(1L, auth("user1", "USER"));

        verify(articleRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete: ADMIN は他ユーザーの記事も削除できる")
    void delete_allowsAdminToDeleteOthersArticle() {
        User author = newUser(1L, "user1", Role.USER);
        Article article = newArticle(1L, "タイトル", "本文", author);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        articleService.delete(1L, auth("admin", "ADMIN"));

        verify(articleRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete: 他ユーザーの記事を削除しようとすると AccessDeniedException")
    void delete_throwsWhenNotOwner() {
        User author = newUser(1L, "user1", Role.USER);
        Article article = newArticle(1L, "タイトル", "本文", author);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        assertThatThrownBy(() -> articleService.delete(1L, auth("other", "USER")))
                .isInstanceOf(AccessDeniedException.class);
        verify(articleRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete: 存在しない場合は ArticleNotFoundException を投げ、deleteById は呼ばれない")
    void delete_throwsWhenNotFound() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.delete(99L, auth("user1", "USER")))
                .isInstanceOf(ArticleNotFoundException.class)
                .hasMessageContaining("id=99");
        verify(articleRepository, never()).deleteById(any());
    }
}
