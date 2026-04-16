import { NavLink, Route, Routes } from 'react-router-dom'
import ArticleCreatePage from './pages/ArticleCreatePage'
import ArticleDetailPage from './pages/ArticleDetailPage'
import ArticleEditPage from './pages/ArticleEditPage'
import ArticleListPage from './pages/ArticleListPage'

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
        </nav>
      </header>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<ArticleListPage />} />
          <Route path="/articles/new" element={<ArticleCreatePage />} />
          <Route path="/articles/:id" element={<ArticleDetailPage />} />
          <Route path="/articles/:id/edit" element={<ArticleEditPage />} />
          <Route path="*" element={<p>404 Not Found</p>} />
        </Routes>
      </main>
    </div>
  )
}

export default App
