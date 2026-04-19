import { NavLink, Route, Routes } from 'react-router-dom'
import ArticleCreatePage from './pages/ArticleCreatePage'
import ArticleDetailPage from './pages/ArticleDetailPage'
import ArticleEditPage from './pages/ArticleEditPage'
import ArticleListPage from './pages/ArticleListPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import { useAuth } from './auth/AuthContext'

// ヘッダー右側：ログイン状態に応じてユーザー名・ログアウト or ログイン・登録リンクを切り替える
function AuthArea() {
  const { user, loading, logout } = useAuth()

  if (loading) return <span style={{ color: '#9ca3af' }}>...</span>

  if (user) {
    return (
      <>
        <span style={{ color: '#374151' }}>{user.username}</span>
        <button onClick={() => void logout()}>ログアウト</button>
      </>
    )
  }

  return (
    <>
      <NavLink to="/login" className={({ isActive }) => (isActive ? 'active' : '')}>
        ログイン
      </NavLink>
      <NavLink to="/register" className={({ isActive }) => (isActive ? 'active' : '')}>
        新規登録
      </NavLink>
    </>
  )
}

// トップレベルコンポーネント：ヘッダーナビゲーション + ルーティング
function App() {
  return (
    <div className="app">
      <header className="app-header">
        <h1>Spring Blog</h1>
        <nav>
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
            記事一覧
          </NavLink>
          <NavLink to="/articles/new" className={({ isActive }) => (isActive ? 'active' : '')}>
            記事を書く
          </NavLink>
          <AuthArea />
        </nav>
      </header>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<ArticleListPage />} />
          <Route path="/articles/new" element={<ArticleCreatePage />} />
          <Route path="/articles/:id" element={<ArticleDetailPage />} />
          <Route path="/articles/:id/edit" element={<ArticleEditPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="*" element={<p>404 Not Found</p>} />
        </Routes>
      </main>
    </div>
  )
}

export default App
