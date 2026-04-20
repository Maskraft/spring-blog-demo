-- article テーブルに author_id を追加し、既存レコードは最初の ADMIN ユーザーに紐付ける
ALTER TABLE article
    ADD COLUMN author_id BIGINT NOT NULL DEFAULT 0;

-- 既存データを最初の ADMIN ユーザー（id が最小のもの）に紐付ける
UPDATE article
SET author_id = (SELECT id FROM users WHERE role = 'ADMIN' ORDER BY id LIMIT 1)
WHERE author_id = 0;

-- DEFAULT 制約を解除し、外部キー制約を追加
ALTER TABLE article
    ALTER COLUMN author_id DROP DEFAULT;

ALTER TABLE article
    ADD CONSTRAINT fk_article_author
        FOREIGN KEY (author_id) REFERENCES users (id);
