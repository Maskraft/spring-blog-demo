import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import ArticleForm from '../components/ArticleForm'
import { getArticle, updateArticle } from '../api/articleApi'
import type { Article, ArticleRequest } from '../types/article'

// 記事編集ページ：既存記事を読み込み、ArticleForm を再利用する
function ArticleEditPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [article, setArticle] = useState<Article | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    setError(null)
    getArticle(Number(id))
      .then(setArticle)
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false))
  }, [id])

  async function handleSubmit(data: ArticleRequest) {
    if (!article) return
    await updateArticle(article.id, data)
    navigate(`/articles/${article.id}`)
  }

  if (loading) return <div className="loading">読み込み中...</div>
  if (error) return <div className="error">読み込み失敗：{error}</div>
  if (!article) return null

  return (
    <section>
      <h2>記事を編集</h2>
      <ArticleForm
        initial={{ title: article.title, content: article.content }}
        onSubmit={handleSubmit}
        submitLabel="保存"
      />
    </section>
  )
}

export default ArticleEditPage
