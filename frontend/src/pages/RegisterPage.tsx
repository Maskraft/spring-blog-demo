import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register as apiRegister } from '../api/authApi'
import { useAuth } from '../auth/AuthContext'
import styles from './AuthForm.module.css'

// 新規登録ページ。バックエンドの制約：username 3〜50 / password 8〜72
function RegisterPage() {
  const { login } = useAuth()
  const navigate = useNavigate()

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
      await apiRegister({ username, password })
      // 登録 API はセッションを発行しないため、続けてログインしてから遷移する
      await login({ username, password })
      navigate('/', { replace: true })
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className={styles.container}>
      <h2>新規登録</h2>
      <form className={styles.form} onSubmit={handleSubmit}>
        <label className={styles.field}>
          <span>ユーザー名（3〜50 文字）</span>
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            minLength={3}
            maxLength={50}
            required
          />
        </label>

        <label className={styles.field}>
          <span>パスワード（8〜72 文字）</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
            minLength={8}
            maxLength={72}
            required
          />
        </label>

        {error && <div className={styles.error}>登録失敗：{error}</div>}

        <div className={styles.actions}>
          <button type="submit" className="primary" disabled={submitting}>
            {submitting ? '送信中...' : '登録'}
          </button>
        </div>

        <p className={styles.footer}>
          すでにアカウントをお持ちの方は <Link to="/login">ログイン</Link>
        </p>
      </form>
    </section>
  )
}

export default RegisterPage
