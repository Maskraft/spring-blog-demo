import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { deleteArticle, listArticles } from '../api/articleApi'
import type { Article } from '../types/article'
import styles from './ArticleListPage.module.css'

// 記事一覧ページ：全記事を表示し、遷移・削除をサポート
function ArticleListPage() {
  const [articles, setArticles] = useState<Article[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    listArticles()
      .then(setArticles)
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  async function handleDelete(id: number) {
    if (!confirm('この記事を削除してもよいですか？')) return
    try {
      await deleteArticle(id)
      setArticles((prev) => prev.filter((a) => a.id !== id))
    } catch (err) {
      alert((err as Error).message)
    }
  }

  if (loading) return <div className="loading">読み込み中...</div>
  if (error) return <div className="error">読み込み失敗：{error}</div>

  if (articles.length === 0) {
    return (
      <div className={styles.empty}>
        <p>記事がまだありません。<Link to="/articles/new">書いてみる</Link>？</p>
      </div>
    )
  }

  return (
    <ul className={styles.list}>
      {articles.map((article) => (
        <li key={article.id} className={styles.item}>
          <div className={styles.itemMain}>
            <Link to={`/articles/${article.id}`} className={styles.title}>
              {article.title}
            </Link>
            <p className={styles.meta}>
              {new Date(article.createdAt).toLocaleString('ja-JP')}
            </p>
          </div>
          <div className={styles.actions}>
            <Link to={`/articles/${article.id}/edit`}>
              <button>編集</button>
            </Link>
            <button className="danger" onClick={() => handleDelete(article.id)}>
              削除
            </button>
          </div>
        </li>
      ))}
    </ul>
  )
}

export default ArticleListPage
