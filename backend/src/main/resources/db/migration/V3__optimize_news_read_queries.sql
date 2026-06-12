CREATE INDEX idx_news_articles_source_published_at
    ON news_articles (source, published_at DESC);

CREATE INDEX idx_news_articles_impact_published_at
    ON news_articles (impact, published_at DESC);

CREATE INDEX idx_news_articles_notification_published_at
    ON news_articles (notification_sent, published_at DESC);
