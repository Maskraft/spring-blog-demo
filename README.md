# Spring Blog

最小構成で動作するブログシステム：Spring Boot 3 + React 19 のフルスタックデモ。

## 技術スタック

- **バックエンド**：Java 21 · Spring Boot 3.5 · Spring Data JPA · Flyway · MySQL 8（開発）/ H2（テスト）
- **フロントエンド**：React 19 · Vite 6 · TypeScript 5.7 · React Router 7
- **パッケージ管理**：Maven(バックエンド)· pnpm(フロントエンド)

## 動作環境

- JDK 21
- Maven 3.9+（または IntelliJ 同梱版を使用）
- Docker Desktop（MySQL をコンテナで起動するため）
- Node.js 20+
- pnpm 10+（未インストールの場合：`npm i -g pnpm` を実行）

## クイックスタート

フロントエンドとバックエンドは別々のターミナルで起動する必要があります。**MySQL → バックエンド → フロントエンド** の順に起動してください。そうしないとフロントエンドからの API 呼び出しが 502 になります。

### 1. MySQL の起動（PowerShell）

```powershell
cd C:\Users\maskr\IdeaProjects\SpringBlog
docker compose up -d
```

初回起動時は MySQL イメージのダウンロードに時間がかかります。`docker compose ps` の `STATUS` が `healthy` になれば準備完了です。MySQL は **3306** ポートを使用します。

### 2. バックエンドの起動（PowerShell）

```powershell
cd C:\Users\maskr\IdeaProjects\SpringBlog
mvn spring-boot:run
```

コンソールに `Started BlogApplication in X.XXX seconds` と表示されれば成功です。バックエンドは **8080** ポートを使用し、起動時に Flyway が `db/migration/V*.sql` を自動適用します。

### 3. フロントエンドの起動（別の PowerShell ウィンドウで）

```powershell
cd C:\Users\maskr\IdeaProjects\SpringBlog\frontend
pnpm install        # 初回起動前に一度実行。以降はスキップ可
pnpm dev
```

成功後、ブラウザで **http://localhost:5173** にアクセスしてください。

### 4. 停止

- フロントエンド／バックエンド：対応するターミナルで `Ctrl + C`。
- MySQL：`docker compose down`（データは保持）／ `docker compose down -v`（データごと破棄）。

## アクセス URL

| サービス       | URL                                    |
|------------|----------------------------------------|
| フロントエンドページ | http://localhost:5173                  |
| バックエンド API | http://localhost:8080/api/v1/articles  |
| MySQL      | localhost:3306（コンテナ名 `spring-blog-mysql`）|

MySQL の接続情報（開発用・`docker-compose.yml` 参照）：
- データベース: `blogdb`
- ユーザー名: `bloguser`
- パスワード: `blogpass`

> テスト実行時は H2 インメモリ DB（MySQL 互換モード）が使用されます。

## REST API 一覧

### 認証 API

| メソッド   | パス                        | 認可    | 説明                                    |
|--------|---------------------------|-------|---------------------------------------|
| POST   | `/api/v1/auth/register`   | 公開    | ユーザー登録（常に `USER` ロールで作成）              |
| POST   | `/api/v1/auth/login`      | 公開    | ログイン。成功時は `JSESSIONID` Cookie を発行     |
| POST   | `/api/v1/auth/logout`     | 認証済み  | ログアウト。セッションと `JSESSIONID` を破棄（※）     |
| GET    | `/api/v1/auth/me`         | 認証済み  | 現在ログイン中のユーザー情報                        |

> ※ `/logout` は `AuthController` ではなく Spring Security のフィルタ（`SecurityConfig`）が直接処理します。

### 記事 API

| メソッド   | パス                | 認可        | 説明   |
|--------|-------------------|-----------|------|
| GET    | `/api/v1/articles`      | 公開        | 記事一覧 |
| GET    | `/api/v1/articles/{id}` | 公開        | 記事詳細 |
| POST   | `/api/v1/articles`      | **ADMIN** | 記事作成 |
| PUT    | `/api/v1/articles/{id}` | **ADMIN** | 記事更新 |
| DELETE | `/api/v1/articles/{id}` | **ADMIN** | 記事削除 |

**記事フィールド**：`id`, `title`, `content`, `createdAt`（サーバー側で自動生成）
**ユーザーフィールド**：`id`, `username`, `role`（`password` はレスポンスに含まれない）

## 認証・認可

- **認証方式**：Session + Cookie（`JSESSIONID`）。Spring Security 6 ベース。
- **パスワード保存**：BCrypt ハッシュ化（strength=10）。平文は DB に保存されない。
- **CSRF 対策**：`CookieCsrfTokenRepository` により `XSRF-TOKEN` Cookie を発行。書き込み系リクエスト（POST/PUT/DELETE/PATCH）では Cookie の値を `X-XSRF-TOKEN` ヘッダに載せて送信する必要がある。GET 系は対象外。
- **セッション固定攻撃対策**：ログイン成功時に旧セッションを破棄し新しいセッション ID を発行する。
- **ロール**：`USER`（`/register` で作成されるデフォルト）と `ADMIN`（記事 CRUD 権限を持つ）の2種類。ロール昇格 API は提供しないため、`USER` を `ADMIN` に昇格させる場合は DB を直接更新する。

### 開発用の初期管理者

起動時に `AdminUserSeeder` が以下の管理者ユーザーを自動投入します（存在しない場合のみ）：

| 項目       | 値         |
|----------|-----------|
| username | `admin`   |
| password | `admin123` |
| role     | `ADMIN`   |

> **本番環境では必ず別の強力なパスワードに変更してください**。`AdminUserSeeder` は `test` プロファイル以外で有効です。

## ディレクトリ構成

```
SpringBlog/
├── pom.xml                          # バックエンド Maven 設定
├── docker-compose.yml               # 開発用 MySQL 8 の定義
├── src/main/
│   ├── java/com/learn/blog/
│   │   ├── BlogApplication.java     # エントリーポイント
│   │   ├── controller/              # REST API
│   │   ├── service/                 # ビジネス層
│   │   ├── repository/              # JPA Repository
│   │   ├── entity/                  # JPA エンティティ
│   │   ├── dto/                     # リクエスト / レスポンス DTO
│   │   ├── security/                # Spring Security 設定・UserDetailsService・admin シード
│   │   └── exception/               # カスタム例外 + グローバル処理
│   └── resources/
│       ├── application.yml          # 共通設定（プロファイル切替・JPA・ログ）
│       ├── application-dev.yml      # dev プロファイル：MySQL + Flyway
│       └── db/migration/            # Flyway マイグレーション（V1__init.sql 等）
├── src/test/resources/
│   └── application-test.yml         # test プロファイル：H2 インメモリ
└── frontend/                        # フロントエンド React + Vite
    ├── package.json
    ├── vite.config.ts               # /api → 8080 のプロキシ設定を含む
    └── src/
        ├── main.tsx / App.tsx          # エントリー + ルーティング（AuthProvider でラップ）
        ├── api/
        │   ├── http.ts                 # fetch 共通ラッパー（credentials: include・CSRF ヘッダ付与）
        │   ├── articleApi.ts           # 記事 API クライアント
        │   └── authApi.ts              # 認証 API クライアント（login/logout/register/me）
        ├── auth/AuthContext.tsx        # 認証状態の Context（user/login/logout/loading）
        ├── types/
        │   ├── article.ts              # 記事型定義
        │   └── auth.ts                 # ユーザー / リクエスト型定義
        ├── components/ArticleForm.tsx  # 作成/編集共用フォーム
        └── pages/                      # 一覧/詳細/作成/編集/ログイン/新規登録
```

## よく使うコマンド

### MySQL（Docker Compose）

```powershell
cd C:\Users\maskr\IdeaProjects\SpringBlog

docker compose up -d                        # MySQL 起動（バックグラウンド）
docker compose ps                           # 状態確認（healthy になるまで待つ）
docker compose logs -f mysql                # ログ追跡
docker compose down                         # 停止（データは保持）
docker compose down -v                      # 停止してデータボリュームも破棄
```

### バックエンド

```powershell
cd C:\Users\maskr\IdeaProjects\SpringBlog

mvn spring-boot:run                         # 開発モードで起動（dev プロファイル）
mvn clean package                           # テスト + API ドキュメント生成 + jar ビルド
mvn clean package -DskipTests               # jar のみビルド（テストをスキップ。スニペットは生成されない）
java -jar target\spring-blog-0.0.1-SNAPSHOT.jar
```

#### Flyway 運用ルール

- 新しいスキーマ変更は `src/main/resources/db/migration/V{次の番号}__{説明}.sql` として**新規ファイルで追加**する。
- **適用済みのマイグレーションファイルは絶対に編集しない**。Flyway が保持するチェックサムと食い違うと起動が失敗する。
- `dev` プロファイルでは `ddl-auto: validate` を使用するため、エンティティと DB スキーマが食い違えば起動時に検出される。

#### テスト・コード品質

```powershell
mvn test                                    # ユニットテスト + アーキテクチャテスト + ドキュメント生成
mvn verify                                  # 上記に加え Spotless check（書式違反でビルド失敗）
mvn spotless:apply                          # Google Java Format (AOSP) で一括整形
mvn spotless:check                          # 書式違反の検出のみ
```

テストの内訳：

| テストクラス | 件数 | 目的 |
|------|------|------|
| `ArticleServiceTest` | 9 | Mockito による Service 層の単体テスト |
| `AuthServiceTest` | 4 | Mockito による認証 Service 層の単体テスト（重複拒否・USER ロール固定・ハッシュ化） |
| `ArticleControllerTest` | 13 | `@WebMvcTest` + MockMvc による HTTP 層テスト（認可: 401/403 含む） |
| `AuthControllerTest` | 11 | `@WebMvcTest` + MockMvc による認証 API テスト（/register, /login, /me, /logout + CSRF 検証） |
| `ArchitectureTest` | 6 | ArchUnit によるアーキテクチャ制約（レイヤー依存・パッケージ配置・`@Transactional` 配置） |
| `ArticleApiDocumentation` | 7 | Spring REST Docs による API ドキュメント用スニペット生成 |

#### API ドキュメント

`mvn package` 実行後、`target/generated-docs/index.html` をブラウザで開くと、各エンドポイントの仕様書を閲覧できます。ソースは [src/main/asciidoc/index.adoc](src/main/asciidoc/index.adoc) に記述されており、HTTP リクエスト例・レスポンス例・フィールド定義は `ArticleApiDocumentation` テストの実行結果から自動生成されます。

### フロントエンド

```powershell
cd C:\Users\maskr\IdeaProjects\SpringBlog\frontend

pnpm install        # 依存関係のインストール
pnpm dev            # 開発サーバー（5173）
pnpm typecheck      # 型チェックのみ実行
pnpm build          # 本番ビルド（成果物は dist/）
pnpm preview        # 本番ビルドのローカルプレビュー
```

## curl によるテスト例(PowerShell)

> Windows PowerShell では `curl` ではなく `curl.exe` を使用してください（前者は `Invoke-WebRequest` のエイリアスです）。バッククォート `` ` `` は行継続文字です。

### 参照系（認証不要）

```powershell
# 一覧
curl.exe "http://localhost:8080/api/v1/articles"

# 詳細
curl.exe "http://localhost:8080/api/v1/articles/1"
```

### 書き込み系（ADMIN 認証 + CSRF トークンが必要）

書き込み系リクエストは **Session Cookie** と **CSRF トークン** の両方を送る必要があります。以下の 3 ステップを踏みます：

```powershell
# 1. ログイン: Cookie (JSESSIONID + XSRF-TOKEN) を cookies.txt に保存する
curl.exe -X POST "http://localhost:8080/api/v1/auth/login" `
  -H "Content-Type: application/json; charset=utf-8" `
  -c cookies.txt `
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"

# 2. cookies.txt から XSRF-TOKEN の値を取り出す (最後のカラム)
$csrf = (Select-String -Path cookies.txt -Pattern "XSRF-TOKEN").Line.Split("`t")[-1]

# 3. 作成: Cookie を送信 (-b) + CSRF トークンをヘッダに載せる
curl.exe -X POST "http://localhost:8080/api/v1/articles" `
  -H "Content-Type: application/json; charset=utf-8" `
  -H "X-XSRF-TOKEN: $csrf" `
  -b cookies.txt -c cookies.txt `
  -d "{\"title\":\"最初の記事\",\"content\":\"Hello Blog\"}"

# 更新
curl.exe -X PUT "http://localhost:8080/api/v1/articles/1" `
  -H "Content-Type: application/json; charset=utf-8" `
  -H "X-XSRF-TOKEN: $csrf" `
  -b cookies.txt -c cookies.txt `
  -d "{\"title\":\"修正後のタイトル\",\"content\":\"本文を修正しました\"}"

# 削除
curl.exe -X DELETE "http://localhost:8080/api/v1/articles/1" -i `
  -H "X-XSRF-TOKEN: $csrf" `
  -b cookies.txt -c cookies.txt

# 現在ログイン中のユーザー
curl.exe "http://localhost:8080/api/v1/auth/me" -b cookies.txt

# ログアウト
curl.exe -X POST "http://localhost:8080/api/v1/auth/logout" `
  -H "X-XSRF-TOKEN: $csrf" `
  -b cookies.txt -c cookies.txt
```

### 新規ユーザーの登録

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/auth/register" `
  -H "Content-Type: application/json; charset=utf-8" `
  -d "{\"username\":\"alice\",\"password\":\"password123\"}"
```

> `/register` で作成されたユーザーは常に `USER` ロールとなり、記事 CRUD はできません。記事を書くには DB を直接更新して `ADMIN` に昇格させるか、seed された `admin` ユーザーでログインしてください。

## 開発時の注意点

- **認証 Cookie とフロントエンド**：ブラウザから `fetch` する場合は必ず `credentials: 'include'` を指定して `JSESSIONID` / `XSRF-TOKEN` を送受信してください。書き込み系リクエストでは Cookie の `XSRF-TOKEN` を読み取り、`X-XSRF-TOKEN` ヘッダに載せる必要があります（これを怠ると 403 Forbidden になります）。
- **CORS 対応**：フロントエンドは `vite.config.ts` の proxy 経由で `/api` リクエストを `localhost:8080` に転送するため、ブラウザから見ると同一オリジンのリクエストになり、**CORS の設定は一切不要**です。本番デプロイ時には別途対応が必要になります（例：Nginx リバースプロキシ、またはフロントエンドをバックエンドの jar に同梱する）。
- **データ永続化**：MySQL のデータは Docker の名前付きボリューム `mysql-data` に永続化されるため、`docker compose down` ではデータは消えません。**完全に初期化したい場合は `docker compose down -v`** を使用してください。
- **プロファイル構成**：アプリは `dev`（MySQL + Flyway）と `test`（H2 インメモリ）の 2 プロファイルを持ちます。`mvn spring-boot:run` はデフォルトで `dev`、`mvn test` は自動で `test` が有効になります。
- **ポート競合**：
  - **3306（MySQL）**：ローカルに既存の MySQL が動作している場合は衝突します。`docker-compose.yml` の `ports` を `"3307:3306"` 等に変更し、併せて `application-dev.yml` の URL ポートも揃えてください。
  - **8080（バックエンド）**：`application.yml` の `server.port` を変更。
  - **5173（フロントエンド）**：`vite.config.ts` の `server.port` を変更（プロキシには影響しません）。
