import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { deletePost, listPosts } from '../api/postApi'
import type { Post } from '../types/post'
import styles from './PostListPage.module.css'

// 記事一覧ページ：全記事を表示し、遷移・削除をサポート
function PostListPage() {
  const [posts, setPosts] = useState<Post[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    listPosts()
      .then(setPosts)
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  async function handleDelete(id: number) {
    if (!confirm('この記事を削除してもよいですか？')) return
    try {
      await deletePost(id)
      setPosts((prev) => prev.filter((p) => p.id !== id))
    } catch (err) {
      alert((err as Error).message)
    }
  }

  if (loading) return <div className="loading">読み込み中...</div>
  if (error) return <div className="error">読み込み失敗：{error}</div>

  if (posts.length === 0) {
    return (
      <div className={styles.empty}>
        <p>記事がまだありません。<Link to="/posts/new">書いてみる</Link>？</p>
      </div>
    )
  }

  return (
    <ul className={styles.list}>
      {posts.map((post) => (
        <li key={post.id} className={styles.item}>
          <div className={styles.itemMain}>
            <Link to={`/posts/${post.id}`} className={styles.title}>
              {post.title}
            </Link>
            <p className={styles.meta}>
              {new Date(post.createdAt).toLocaleString('ja-JP')}
            </p>
          </div>
          <div className={styles.actions}>
            <Link to={`/posts/${post.id}/edit`}>
              <button>編集</button>
            </Link>
            <button className="danger" onClick={() => handleDelete(post.id)}>
              削除
            </button>
          </div>
        </li>
      ))}
    </ul>
  )
}

export default PostListPage
