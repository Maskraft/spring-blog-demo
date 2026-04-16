import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import type { ArticleRequest } from '../types/article'
import styles from './ArticleForm.module.css'

interface Props {
  initial?: ArticleRequest
  submitLabel: string
  onSubmit: (data: ArticleRequest) => Promise<void>
}

// 作成・編集共通フォームコンポーネント
function ArticleForm({ initial, submitLabel, onSubmit }: Props) {
  const navigate = useNavigate()
  const [title, setTitle] = useState(initial?.title ?? '')
  const [content, setContent] = useState(initial?.content ?? '')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (submitting) return
    setSubmitting(true)
    setError(null)
    try {
      await onSubmit({ title, content })
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <label className={styles.field}>
        <span>タイトル</span>
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="タイトルを入力してください"
          maxLength={200}
          required
        />
      </label>

      <label className={styles.field}>
        <span>本文</span>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="本文を入力してください"
          required
        />
      </label>

      {error && <div className={styles.error}>送信失敗：{error}</div>}

      <div className={styles.actions}>
        <button type="submit" className="primary" disabled={submitting}>
          {submitting ? '送信中...' : submitLabel}
        </button>
        <button type="button" onClick={() => navigate(-1)}>キャンセル</button>
      </div>
    </form>
  )
}

export default ArticleForm
