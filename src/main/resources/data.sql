-- カテゴリーテーブルデータ
INSERT INTO genres(genre_name, is_income) VALUES('収入', TRUE);
INSERT INTO genres(genre_name, is_income) VALUES('固定費', FALSE);
INSERT INTO genres(genre_name, is_income) VALUES('変動費', FALSE);
INSERT INTO genres(genre_name, is_income) VALUES('その他の収入', TRUE);
INSERT INTO genres(genre_name, is_income) VALUES('その他の支出', FALSE);
-- ユーザテーブルデータ
INSERT INTO users(user_name,email,password) VALUES('田中太郎', 'tanaka@aaa.com','himitu');
INSERT INTO users(user_name,email,password) VALUES('鈴木一郎', 'suzuki@aaa.com','himitu');
-- 項目テーブルデータ
INSERT INTO items(item_name, user_id, genre_id, price, add_date) VALUES('食事代', 1, 3, 1200, '2026/05/01');
INSERT INTO items(item_name, user_id, genre_id, price, add_date) VALUES('家賃', 1, 2, 60000, '2026/05/10');
INSERT INTO items(item_name, user_id, genre_id, price, add_date) VALUES('ラーメン', 1, 3, 1500, '2026/05/15');
INSERT INTO items(item_name, user_id, genre_id, price, add_date) VALUES('収入', 1, 1, 250000, '2026/05/20');
INSERT INTO items(item_name, user_id, genre_id, price, add_date) VALUES('Yさんへの誕プレ', 1, 5, 10000, '2026/05/23');
INSERT INTO items(item_name, user_id, genre_id, price, add_date) VALUES('株の収益', 1, 4, 20000, '2026/05/23');