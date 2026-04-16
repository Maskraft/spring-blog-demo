// バックエンドが返す記事オブジェクト
export interface Post {
  id: number
  title: string
  content: string
  // Spring Boot はデフォルトで LocalDateTime を ISO 文字列にシリアライズする
  createdAt: string
}

// 記事作成 / 更新用リクエストボディ
export interface PostRequest {
  title: string
  content: string
}

// バックエンドのエラーレスポンス。GlobalExceptionHandler と対応する
export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
}
