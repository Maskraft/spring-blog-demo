import { request } from './http'
import type { LoginRequest, RegisterRequest, User } from '../types/auth'

const BASE_URL = '/api/v1/auth'

export function register(data: RegisterRequest): Promise<User> {
  return request<User>(`${BASE_URL}/register`, { method: 'POST', body: data })
}

export function login(data: LoginRequest): Promise<User> {
  return request<User>(`${BASE_URL}/login`, { method: 'POST', body: data })
}

export function logout(): Promise<void> {
  return request<void>(`${BASE_URL}/logout`, { method: 'POST' })
}

// 現在ログイン中のユーザー。未認証時は 401 になるためエラーをスローする
export function fetchMe(): Promise<User> {
  return request<User>(`${BASE_URL}/me`)
}
