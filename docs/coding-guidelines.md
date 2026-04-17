# コーディング規約

## プロジェクト構造

```
src/main/java/com/{会社ドメイン}/{プロジェクト名}/
├── controller/    # REST API
├── service/       # ビジネスロジック
├── repository/    # データアクセス
├── entity/        # エンティティ（JPA）
├── dto/           # リクエスト/レスポンスオブジェクト
├── config/        # 設定クラス
└── exception/     # 例外処理
```

## 命名規則

| 種類 | 規則 | 例 |
|------|------|-----|
| メソッド | キャメルケース | `findById`, `createArticle` |
| クラス | アッパーキャメルケース | `ArticleController` |
| 定数 | スネークケース | `MAX_RETRY_COUNT` |
| パッケージ | 全て小文字 | `com.example.blog` |

## API設計規則

- RESTful原則に基づくURL設計（例: `/api/articles`）
- HTTPメソッドの適切な使用（GET/POST/PUT/DELETE）
- バージョニング（例: `/api/v1/articles`）
- エラーレスポンスは統一形式で返す

## 実装ルール

### Spring アノテーション
- `@Service`/`@Repository` などSpringアノテーションは明示的に付ける
- `@Transactional` はサービス層に記述する

### バリデーション
- `@Valid` + Bean Validation を使用
- エラーハンドリングは `@ControllerAdvice` で統一

### DTO の使用
- リクエスト/レスポンスは DTO クラスを作成し、エンティティを直接公開しない
- エンティティの変更が API に影響を与えないようにする

## コードスタイル

- インデント: スペース4つ
- 行末改行: Unix形式（LF）
- 文字コード: UTF-8
- 1行の最大文字数: 120

## ツール

| ツール | 用途 |
|--------|------|
| Google Java Style Guide | コードスタイル |
| Spotless / Checkstyle | 自動フォーマット・チェック |
| Spring REST Docs | APIドキュメント生成 |
| ArchUnit | アーキテクチャ制約のテスト |
