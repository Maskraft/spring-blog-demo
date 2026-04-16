import { useNavigate } from 'react-router-dom'
import ArticleForm from '../components/ArticleForm'
import { createArticle } from '../api/articleApi'
import type { ArticleRequest } from '../types/article'

// 記事作成ページ
function ArticleCreatePage() {
  const navigate = useNavigate()

  async function handleSubmit(data: ArticleRequest) {
    const created = await createArticle(data)
    navigate(`/articles/${created.id}`)
  }

  return (
    <section>
      <h2>記事を書く</h2>
      <ArticleForm onSubmit={handleSubmit} submitLabel="投稿" />
    </section>
  )
}

export default ArticleCreatePage
