# コーディング規約

本プロジェクトは Spring Boot (Java 21) のバックエンドと、React + TypeScript + Vite のフロントエンドで構成される。以下、それぞれの規約を示す。

## 共通事項

- 文字コード: UTF-8
- 行末改行: Unix 形式（LF）
- コメント・ドキュメントは日本語で記述する
- コミットメッセージも日本語を許容する

---

## バックエンド（Spring Boot）

### プロジェクト構造

本プロジェクトのルートパッケージは `com.learn.blog` である。

```
src/main/java/com/learn/blog/
├── BlogApplication.java   # エントリポイント
├── controller/            # REST API コントローラ
├── service/               # ビジネスロジック
├── repository/            # データアクセス（Spring Data JPA）
├── entity/                # JPA エンティティ
├── dto/                   # リクエスト / レスポンスオブジェクト
├── security/              # Spring Security 設定・認証関連
└── exception/             # 例外処理（@ControllerAdvice 等）
```

新規プロジェクトでは `com.{会社ドメイン}.{プロジェクト名}` とする。

### 命名規則

| 種類 | 規則 | 例 |
|------|------|-----|
| メソッド | キャメルケース | `findById`, `createArticle` |
| クラス | アッパーキャメルケース | `ArticleController` |
| 定数 | スネークケース（大文字） | `MAX_RETRY_COUNT` |
| パッケージ | 全て小文字 | `com.learn.blog` |

### API 設計規則

- RESTful 原則に基づく URL 設計（例: `/api/v1/articles`）
- HTTP メソッドを適切に使用する（GET / POST / PUT / DELETE）
- バージョニング必須（例: `/api/v1/articles`）
- エラーレスポンスは `GlobalExceptionHandler` 経由で統一形式（`ApiError`）で返す

### 実装ルール

#### Spring アノテーション
- `@Service` / `@Repository` などの Spring アノテーションは明示的に付与する
- `@Transactional` はサービス層に記述する

#### バリデーション
- `@Valid` + Bean Validation を使用する
- エラーハンドリングは `@ControllerAdvice` で統一する

#### DTO の使用
- リクエスト / レスポンスは DTO クラスを作成し、エンティティを直接公開しない
- エンティティの変更が API に影響を与えないようにする

#### セキュリティ
- 認証はセッション + Cookie 方式（Spring Security）
- CSRF 対策として `CookieCsrfTokenRepository` を用い、`XSRF-TOKEN` Cookie を発行する
- 認可が必要なエンドポイントは `@PreAuthorize` で制御する

### コードスタイル

- インデント: スペース 4 つ
- 1 行の最大文字数: 120
- Google Java Style Guide に準拠
- Spotless により自動整形される（CI でチェックされる）

### ツール

| ツール | 用途 |
|--------|------|
| Google Java Style Guide | コードスタイルの基準 |
| Spotless | 自動フォーマット（Maven 実行時適用） |
| Spring REST Docs | API ドキュメント生成 |
| ArchUnit | アーキテクチャ制約のテスト |

---

## フロントエンド（React + TypeScript）

### 技術スタック

- React 19 + TypeScript 5.7
- Vite 6（ビルド / 開発サーバ）
- React Router v7（クライアントルーティング）
- CSS Modules（コンポーネント単位のスタイル）
- パッケージマネージャ: pnpm

### プロジェクト構造

```
frontend/src/
├── main.tsx              # エントリポイント
├── App.tsx               # ルーティング・ヘッダー
├── index.css             # グローバル CSS
├── api/                  # バックエンド API 呼び出し層
│   ├── http.ts           # fetch ラッパー（CSRF / Cookie / エラー処理）
│   ├── articleApi.ts
│   └── authApi.ts
├── auth/                 # 認証状態管理
│   └── AuthContext.tsx
├── components/           # 再利用可能な UI コンポーネント
├── pages/                # ルート単位の画面コンポーネント
└── types/                # ドメイン型定義（バックエンド DTO と対応）
```

### 命名規則

| 種類 | 規則 | 例 |
|------|------|-----|
| コンポーネント / ファイル | アッパーキャメルケース + `.tsx` | `ArticleForm.tsx`, `LoginPage.tsx` |
| フック | `use` 接頭辞 + キャメルケース | `useAuth` |
| 関数 / 変数 | キャメルケース | `fetchMe`, `handleSubmit` |
| 型 / インターフェース | アッパーキャメルケース | `Article`, `AuthContextValue` |
| CSS Modules ファイル | 対象コンポーネントと同名 + `.module.css` | `ArticleForm.module.css` |
| API モジュール | `{リソース}Api.ts` | `articleApi.ts` |

### 実装ルール

#### API 呼び出し
- HTTP 通信は `api/http.ts` の `request<T>()` を必ず経由する
- 直接 `fetch` を書かない（CSRF トークン / Cookie / エラー処理の一元化のため）
- リソースごとに `api/{resource}Api.ts` を作成し、関数形式でエクスポートする
- バックエンドの DTO に対応する型を `types/` に定義する

#### 認証・状態管理
- 認証状態は `AuthContext` で一元管理する
- コンポーネントからは `useAuth()` フック経由でアクセスする
- `AuthProvider` の外側で `useAuth` を呼ぶとエラーになる
- グローバルな状態ライブラリ（Redux 等）は現時点では導入しない

#### コンポーネント設計
- 関数コンポーネント + フックのみを使用する（クラスコンポーネント禁止）
- `pages/` は画面単位のコンテナ、`components/` は再利用可能な部品
- Props は `interface Props` で明示的に型付けする
- `default export` を画面・コンポーネントファイルで使用する

#### スタイリング
- コンポーネント固有のスタイルは CSS Modules（`*.module.css`）を使用する
- グローバルなレイアウト・リセットは `index.css` にのみ記述する
- インラインスタイルは小さな条件付き表示に限り許容する

#### TypeScript
- `any` の使用禁止。外部由来の不明な値は `unknown` を経由する
- 型インポートは `import type { ... }` を使用する
- `strict` モードを有効にする（`tsconfig` で設定済み）

#### ルーティング
- すべてのルート定義は `App.tsx` に集約する
- URL パラメータは `useParams` で取得し、数値化時は `Number()` で明示的に変換する

### コードスタイル

- インデント: スペース 2 つ（TypeScript / JSX の慣例）
- クォート: シングルクォート
- 末尾セミコロン: なし
- 末尾カンマ: 複数行は付ける
- 1 行の最大文字数: 100

### スクリプト

| コマンド | 用途 |
|----------|------|
| `pnpm dev` | 開発サーバ起動（Vite） |
| `pnpm build` | 型チェック + 本番ビルド |
| `pnpm typecheck` | 型チェックのみ |
| `pnpm preview` | ビルド成果物のプレビュー |
