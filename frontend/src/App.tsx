import { NavLink, Route, Routes } from 'react-router-dom'
import PostCreatePage from './pages/PostCreatePage'
import PostDetailPage from './pages/PostDetailPage'
import PostEditPage from './pages/PostEditPage'
import PostListPage from './pages/PostListPage'

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
          <NavLink to="/posts/new" className={({ isActive }) => (isActive ? 'active' : '')}>
            記事を書く
          </NavLink>
        </nav>
      </header>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<PostListPage />} />
          <Route path="/posts/new" element={<PostCreatePage />} />
          <Route path="/posts/:id" element={<PostDetailPage />} />
          <Route path="/posts/:id/edit" element={<PostEditPage />} />
          <Route path="*" element={<p>404 Not Found</p>} />
        </Routes>
      </main>
    </div>
  )
}

export default App
