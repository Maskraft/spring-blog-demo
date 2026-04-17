# Spring Blog

最小構成で動作するブログシステム：Spring Boot 3 + React 19 のフルスタックデモ。

## 技術スタック

- **バックエンド**：Java 21 · Spring Boot 3.4 · Spring Data JPA · H2 インメモリデータベース
- **フロントエンド**：React 19 · Vite 6 · TypeScript 5.7 · React Router 7
- **パッケージ管理**：Maven(バックエンド)· pnpm(フロントエンド)

## 動作環境

- JDK 21
- Maven 3.9+（または IntelliJ 同梱版を使用）
- Node.js 20+
- pnpm 10+（未インストールの場合：`npm i -g pnpm` を実行）

## クイックスタート

フロントエンドとバックエンドは別々のターミナルで起動する必要があります。**先にバックエンドを起動**してください。そうしないとフロントエンドからの API 呼び出しが 502 になります。

### 1. バックエンドの起動（PowerShell）

```powershell
cd C:\Users\maskr\IdeaProjects\SpringBlog
mvn spring-boot:run
```

コンソールに `Started BlogApplication in X.XXX seconds` と表示されれば成功です。バックエンドは **8080** ポートを使用します。

### 2. フロントエンドの起動（別の PowerShell ウィンドウで）

```powershell
cd C:\Users\maskr\IdeaProjects\SpringBlog\frontend
pnpm install        # 初回起動前に一度実行。以降はスキップ可
pnpm dev
```

成功後、ブラウザで **http://localhost:5173** にアクセスしてください。

### 3. 停止

対応するターミナルで `Ctrl + C` を押してください。

## アクセス URL

| サービス       | URL                              |
|------------|----------------------------------|
| フロントエンドページ | http://localhost:5173            |
| バックエンド API | http://localhost:8080/api/v1/articles |
| H2 コンソール   | http://localhost:8080/h2-console |

H2 コンソールのログイン情報：
- JDBC URL: `jdbc:h2:mem:blogdb`
- ユーザー名: `sa`
- パスワード: 空欄

## REST API 一覧

| メソッド   | パス                | 説明   |
|--------|-------------------|------|
| GET    | `/api/v1/articles`      | 記事一覧 |
| GET    | `/api/v1/articles/{id}` | 記事詳細 |
| POST   | `/api/v1/articles`      | 記事作成 |
| PUT    | `/api/v1/articles/{id}` | 記事更新 |
| DELETE | `/api/v1/articles/{id}` | 記事削除 |

**フィールド**：`id`, `title`, `content`, `createdAt`（サーバー側で自動生成）

## ディレクトリ構成

```
SpringBlog/
├── pom.xml                          # バックエンド Maven 設定
├── src/main/
│   ├── java/com/learn/blog/
│   │   ├── BlogApplication.java     # エントリーポイント
│   │   ├── controller/              # REST API
│   │   ├── service/                 # ビジネス層
│   │   ├── repository/              # JPA Repository
│   │   ├── entity/                  # JPA エンティティ
│   │   ├── dto/                     # リクエスト / レスポンス DTO
│   │   └── exception/               # カスタム例外 + グローバル処理
│   └── resources/
│       └── application.yml          # 設定（H2 / JPA / ポート）
└── frontend/                        # フロントエンド React + Vite
    ├── package.json
    ├── vite.config.ts               # /api → 8080 のプロキシ設定を含む
    └── src/
        ├── main.tsx / App.tsx       # エントリー + ルーティング
        ├── api/articleApi.ts           # fetch ラッパー
        ├── types/article.ts            # 型定義
        ├── components/ArticleForm.tsx  # 作成/編集共用フォーム
        └── pages/                      # 一覧/詳細/作成/編集
```

## よく使うコマンド

### バックエンド

```powershell
cd C:\Users\maskr\IdeaProjects\SpringBlog

mvn spring-boot:run                         # 開発モードで起動
mvn clean package -DskipTests               # jar をビルド
java -jar target\spring-blog-0.0.1-SNAPSHOT.jar
```

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

```powershell
# 作成
curl.exe -X POST "http://localhost:8080/api/v1/articles" `
  -H "Content-Type: application/json; charset=utf-8" `
  -d "{\"title\":\"最初の記事\",\"content\":\"Hello Blog\"}"

# 一覧
curl.exe "http://localhost:8080/api/v1/articles"

# 詳細
curl.exe "http://localhost:8080/api/v1/articles/1"

# 更新
curl.exe -X PUT "http://localhost:8080/api/v1/articles/1" `
  -H "Content-Type: application/json; charset=utf-8" `
  -d "{\"title\":\"修正後のタイトル\",\"content\":\"本文を修正しました\"}"

# 削除
curl.exe -X DELETE "http://localhost:8080/api/v1/articles/1" -i
```

## 開発時の注意点

- **CORS 対応**：フロントエンドは `vite.config.ts` の proxy 経由で `/api` リクエストを `localhost:8080` に転送するため、ブラウザから見ると同一オリジンのリクエストになり、**CORS の設定は一切不要**です。本番デプロイ時には別途対応が必要になります（例：Nginx リバースプロキシ、またはフロントエンドをバックエンドの jar に同梱する）。
- **データ永続化**：H2 は現在インメモリモードで動作しているため、**バックエンドを再起動するとデータは消えます**。MySQL へ移行する場合は `application.yml` の `datasource` と `pom.xml` の依存関係を変更するだけで済みます。
- **JPA DDL 戦略**：`ddl-auto=create-drop` を使用しており、起動ごとに自動でテーブルを作り直します。MySQL に切り替える際は `update` に変更するか、Flyway / Liquibase の導入を推奨します。
- **ポート競合**：8080 または 5173 が使用中の場合、それぞれ `application.yml` の `server.port` と `vite.config.ts` の `server.port` を変更してください（フロントエンドのポートを変更してもプロキシには影響しません）。
