import { request } from './http'
import type { Article, ArticleRequest } from '../types/article'

const BASE_URL = '/api/v1/articles'

export function listArticles(): Promise<Article[]> {
  return request<Article[]>(BASE_URL)
}

export function getArticle(id: number): Promise<Article> {
  return request<Article>(`${BASE_URL}/${id}`)
}

export function createArticle(data: ArticleRequest): Promise<Article> {
  return request<Article>(BASE_URL, { method: 'POST', body: data })
}

export function updateArticle(id: number, data: ArticleRequest): Promise<Article> {
  return request<Article>(`${BASE_URL}/${id}`, { method: 'PUT', body: data })
}

export function deleteArticle(id: number): Promise<void> {
  return request<void>(`${BASE_URL}/${id}`, { method: 'DELETE' })
}
