import type { ApiError } from '../types/article'

// Cookie から XSRF-TOKEN を読み取る。Spring Security の CookieCsrfTokenRepository が発行する
function readCsrfToken(): string | null {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/)
  return match ? decodeURIComponent(match[1]) : null
}

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

interface RequestOptions {
  method?: string
  body?: unknown
}

// 共通 fetch ラッパー：Session Cookie 送信 + CSRF トークンヘッダ付与 + JSON 化
export async function request<T>(url: string, options: RequestOptions = {}): Promise<T> {
  const method = options.method ?? 'GET'
  const headers: Record<string, string> = {}

  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  // GET/HEAD 以外は CSRF トークンが必須。Cookie にトークンが無い状態でサーバに投げると
  // 403 が返るだけでなく「なぜ失敗したか」が分かりにくいため、ここで明示的にエラーをスローする。
  // login / register は SecurityConfig 側で CSRF 対象外のため、トークン無しでも送信を許可する
  if (method !== 'GET' && method !== 'HEAD') {
    const token = readCsrfToken()
    const csrfExempt = url === '/api/v1/auth/login' || url === '/api/v1/auth/register'
    if (token) {
      headers['X-XSRF-TOKEN'] = token
    } else if (!csrfExempt) {
      throw new Error(
        'CSRF トークンが見つかりません。ログインし直すか、ページを再読み込みしてください。',
      )
    }
  }

  const res = await fetch(url, {
    method,
    headers,
    credentials: 'include',
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  })
  return handleResponse<T>(res)
}
