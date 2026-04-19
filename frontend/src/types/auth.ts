// バックエンドの Role enum と対応する
export type Role = 'USER' | 'ADMIN'

// バックエンドの UserResponse と対応する
export interface User {
  id: number
  username: string
  role: Role
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
}
