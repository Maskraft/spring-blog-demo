package com.learn.blog.service;

import com.learn.blog.dto.ArticleRequest;
import com.learn.blog.dto.ArticleResponse;
import com.learn.blog.entity.Article;
import com.learn.blog.exception.ArticleNotFoundException;
import com.learn.blog.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// ArticleService のビジネスロジックを Mockito で検証する単体テスト
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleService articleService;

    // テスト用の Article を作成（id と createdAt を明示的に設定）
    private Article newArticle(Long id, String title, String content) {
        Article article = new Article(title, content);
        article.setId(id);
        article.setCreatedAt(LocalDateTime.of(2026, 4, 17, 10, 0));
        return article;
    }

    @Test
    @DisplayName("findAll: 全件を ArticleResponse に変換して返す")
    void findAll_returnsAllArticlesAsResponse() {
        Article a1 = newArticle(1L, "タイトル1", "本文1");
        Article a2 = newArticle(2L, "タイトル2", "本文2");
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
        Article article = newArticle(1L, "タイトル", "本文");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        ArticleResponse result = articleService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("タイトル");
        assertThat(result.content()).isEqualTo("本文");
        assertThat(result.createdAt()).isEqualTo(LocalDateTime.of(2026, 4, 17, 10, 0));
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
        ArticleRequest request = new ArticleRequest("新しいタイトル", "新しい本文");
        Article saved = newArticle(10L, "新しいタイトル", "新しい本文");
        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        ArticleResponse result = articleService.create(request);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("新しいタイトル");
        assertThat(result.content()).isEqualTo("新しい本文");
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    @Test
    @DisplayName("update: 既存エンティティを上書きして ArticleResponse を返す")
    void update_modifiesExistingAndReturnsResponse() {
        Article existing = newArticle(1L, "旧タイトル", "旧本文");
        ArticleRequest request = new ArticleRequest("新タイトル", "新本文");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(articleRepository.save(existing)).thenReturn(existing);

        ArticleResponse result = articleService.update(1L, request);

        assertThat(result.title()).isEqualTo("新タイトル");
        assertThat(result.content()).isEqualTo("新本文");
        // setter によってエンティティが書き換わっていることを確認
        assertThat(existing.getTitle()).isEqualTo("新タイトル");
        assertThat(existing.getContent()).isEqualTo("新本文");
    }

    @Test
    @DisplayName("update: 対象が存在しない場合は ArticleNotFoundException を投げ、save は呼ばれない")
    void update_throwsWhenNotFound() {
        ArticleRequest request = new ArticleRequest("タイトル", "本文");
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.update(99L, request))
                .isInstanceOf(ArticleNotFoundException.class);
        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete: 存在する場合は deleteById が呼ばれる")
    void delete_removesWhenExists() {
        when(articleRepository.existsById(1L)).thenReturn(true);

        articleService.delete(1L);

        verify(articleRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete: 存在しない場合は ArticleNotFoundException を投げ、deleteById は呼ばれない")
    void delete_throwsWhenNotFound() {
        when(articleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> articleService.delete(99L))
                .isInstanceOf(ArticleNotFoundException.class)
                .hasMessageContaining("id=99");
        verify(articleRepository, never()).deleteById(any());
    }
}
