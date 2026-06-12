\set ON_ERROR_STOP on

-- Read-only query-plan baseline. Run with a read-only PostgreSQL account.
BEGIN;
SET TRANSACTION READ ONLY;

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT *
FROM news_articles
ORDER BY published_at DESC
LIMIT 20;

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT *
FROM news_articles
WHERE published_at >= CURRENT_DATE
  AND published_at < CURRENT_DATE + INTERVAL '1 day'
ORDER BY published_at DESC;

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT *
FROM news_articles
WHERE source = 'openai'
ORDER BY published_at DESC
LIMIT 20;

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT *
FROM news_articles
WHERE impact = 'HIGH'
ORDER BY published_at DESC
LIMIT 20;

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT *
FROM news_articles
WHERE notification_sent = FALSE
  AND published_at >= CURRENT_DATE
  AND published_at < CURRENT_DATE + INTERVAL '1 day'
ORDER BY published_at DESC;

ROLLBACK;
