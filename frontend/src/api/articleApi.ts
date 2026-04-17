import type { ApiError, Article, ArticleRequest } from '../types/article'

const BASE_URL = '/api/v1/articles'

// レスポンス統一処理：2xx 以外はバックエンドの message を持つエラーをスローする
async function handleResponse<T>(res: Response): Promise<T> {
  if (res.ok) {
    if (res.status === 204) {
      return undefined as T
    }
    return res.json() as Promise<T>
  }

  let message = `${res.status} ${res.statusText}`
  try {
    const body = (await res.json()) as ApiError
    if (body.message) {
      message = body.message
    }
  } catch {
    // レスポンスボディが JSON でない場合は無視する
  }
  throw new Error(message)
}

export function listArticles(): Promise<Article[]> {
  return fetch(BASE_URL).then((res) => handleResponse<Article[]>(res))
}

export function getArticle(id: number): Promise<Article> {
  return fetch(`${BASE_URL}/${id}`).then((res) => handleResponse<Article>(res))
}

export function createArticle(data: ArticleRequest): Promise<Article> {
  return fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  }).then((res) => handleResponse<Article>(res))
}

export function updateArticle(id: number, data: ArticleRequest): Promise<Article> {
  return fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  }).then((res) => handleResponse<Article>(res))
}

export function deleteArticle(id: number): Promise<void> {
  return fetch(`${BASE_URL}/${id}`, { method: 'DELETE' }).then((res) =>
    handleResponse<void>(res),
  )
}
