import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import styles from './AuthForm.module.css'

// ログインページ。ログイン成功後は遷移元（state.from）または記事一覧へ戻る
function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  // オープンリダイレクト対策：from は自サイト内の相対パスのみ許可する。
  // 「/」始まり、かつ「//」「/\」で始まらない（プロトコル相対 URL / バックスラッシュ経由の外部遷移を拒否）
  const rawFrom = (location.state as { from?: string } | null)?.from
  const from =
    rawFrom && rawFrom.startsWith('/') && !rawFrom.startsWith('//') && !rawFrom.startsWith('/\\')
      ? rawFrom
      : '/'

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (submitting) return
    setSubmitting(true)
    setError(null)
    try {
      await login({ username, password })
      navigate(from, { replace: true })
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className={styles.container}>
      <h2>ログイン</h2>
      <form className={styles.form} onSubmit={handleSubmit}>
        <label className={styles.field}>
          <span>ユーザー名</span>
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            required
          />
        </label>

        <label className={styles.field}>
          <span>パスワード</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </label>

        {error && <div className={styles.error}>ログイン失敗：{error}</div>}

        <div className={styles.actions}>
          <button type="submit" className="primary" disabled={submitting}>
            {submitting ? '送信中...' : 'ログイン'}
          </button>
        </div>

        <p className={styles.footer}>
          アカウントをお持ちでない方は <Link to="/register">新規登録</Link>
        </p>
      </form>
    </section>
  )
}

export default LoginPage
