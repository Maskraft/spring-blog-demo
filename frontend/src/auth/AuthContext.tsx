import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { fetchMe, login as apiLogin, logout as apiLogout } from '../api/authApi'
import type { LoginRequest, User } from '../types/auth'

interface AuthContextValue {
  user: User | null
  // 初回 /me 確認が完了するまで true。画面側はこの間ログインリンクを出さない判断ができる
  loading: boolean
  login: (data: LoginRequest) => Promise<void>
  logout: () => Promise<void>
  // 登録直後に Context のユーザー状態を反映するため公開する
  setUser: (user: User | null) => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  // ページリロード時にセッションが生きているか確認する
  useEffect(() => {
    fetchMe()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false))
  }, [])

  const login = useCallback(async (data: LoginRequest) => {
    const u = await apiLogin(data)
    setUser(u)
  }, [])

  const logout = useCallback(async () => {
    await apiLogout()
    setUser(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ user, loading, login, logout, setUser }),
    [user, loading, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth は AuthProvider の内側で呼び出してください')
  }
  return ctx
}
