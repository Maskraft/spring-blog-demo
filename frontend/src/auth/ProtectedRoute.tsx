import { Navigate, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from './AuthContext'

interface Props {
  children: ReactNode
}

// 認証必須ルートのラッパー。未ログインなら /login にリダイレクトし、遷移元を state.from で渡す
function ProtectedRoute({ children }: Props) {
  const { user, loading } = useAuth()
  const location = useLocation()

  // 初回 /me 確認中はリダイレクト判定を保留する（ログイン中なのに /login へ飛ばすのを防ぐ）
  if (loading) {
    return <p style={{ color: '#9ca3af' }}>...</p>
  }

  if (!user) {
    const from = `${location.pathname}${location.search}`
    return <Navigate to="/login" replace state={{ from }} />
  }

  return <>{children}</>
}

export default ProtectedRoute
