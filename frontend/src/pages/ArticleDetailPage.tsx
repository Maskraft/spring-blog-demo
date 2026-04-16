import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { deleteArticle, getArticle } from '../api/articleApi'
import type { Article } from '../types/article'

// 記事詳細ページ：単一記事を表示し、編集・削除をサポート
function ArticleDetailPage() {
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

  async function handleDelete() {
    if (!article) return
    if (!confirm('この記事を削除してもよいですか？')) return
    try {
      await deleteArticle(article.id)
      navigate('/')
    } catch (err) {
      alert((err as Error).message)
    }
  }

  if (loading) return <div className="loading">読み込み中...</div>
  if (error) return <div className="error">読み込み失敗：{error}</div>
  if (!article) return null

  return (
    <article>
      <h2 style={{ marginBottom: '0.4rem' }}>{article.title}</h2>
      <p style={{ color: '#6b7280', fontSize: '0.9rem', marginTop: 0 }}>
        投稿日時：{new Date(article.createdAt).toLocaleString('ja-JP')}
      </p>
      <div
        style={{
          whiteSpace: 'pre-wrap',
          padding: '1rem 0',
          borderTop: '1px solid #e5e7eb',
        }}
      >
        {article.content}
      </div>
      <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1.5rem' }}>
        <Link to={`/articles/${article.id}/edit`}><button>編集</button></Link>
        <button className="danger" onClick={handleDelete}>削除</button>
        <Link to="/"><button>一覧に戻る</button></Link>
      </div>
    </article>
  )
}

export default ArticleDetailPage
