import { useNavigate } from 'react-router-dom'
import PostForm from '../components/PostForm'
import { createPost } from '../api/postApi'
import type { PostRequest } from '../types/post'

// 記事作成ページ
function PostCreatePage() {
  const navigate = useNavigate()

  async function handleSubmit(data: PostRequest) {
    const created = await createPost(data)
    navigate(`/posts/${created.id}`)
  }

  return (
    <section>
      <h2>記事を書く</h2>
      <PostForm onSubmit={handleSubmit} submitLabel="投稿" />
    </section>
  )
}

export default PostCreatePage
