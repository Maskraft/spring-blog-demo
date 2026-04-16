import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import PostForm from '../components/PostForm'
import { getPost, updatePost } from '../api/postApi'
import type { Post, PostRequest } from '../types/post'

// 記事編集ページ：既存記事を読み込み、PostForm を再利用する
function PostEditPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [post, setPost] = useState<Post | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    setError(null)
    getPost(Number(id))
      .then(setPost)
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false))
  }, [id])

  async function handleSubmit(data: PostRequest) {
    if (!post) return
    await updatePost(post.id, data)
    navigate(`/posts/${post.id}`)
  }

  if (loading) return <div className="loading">読み込み中...</div>
  if (error) return <div className="error">読み込み失敗：{error}</div>
  if (!post) return null

  return (
    <section>
      <h2>記事を編集</h2>
      <PostForm
        initial={{ title: post.title, content: post.content }}
        onSubmit={handleSubmit}
        submitLabel="保存"
      />
    </section>
  )
}

export default PostEditPage
