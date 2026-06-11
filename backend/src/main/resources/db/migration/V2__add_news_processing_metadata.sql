ALTER TABLE news_articles ADD COLUMN summary TEXT;
ALTER TABLE news_articles ADD COLUMN impact VARCHAR(16) NOT NULL DEFAULT 'PENDING';
ALTER TABLE news_articles ADD COLUMN developer_view TEXT;
ALTER TABLE news_articles ADD COLUMN collection_status VARCHAR(32) NOT NULL DEFAULT 'COLLECTED';
ALTER TABLE news_articles ADD COLUMN notification_sent BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE news_article_keywords (
    article_id BIGINT NOT NULL,
    keyword VARCHAR(120) NOT NULL,
    PRIMARY KEY (article_id, keyword),
    CONSTRAINT fk_news_article_keywords_article
        FOREIGN KEY (article_id) REFERENCES news_articles (id) ON DELETE CASCADE
);

CREATE TABLE news_article_related_sources (
    article_id BIGINT NOT NULL,
    source VARCHAR(120) NOT NULL,
    PRIMARY KEY (article_id, source),
    CONSTRAINT fk_news_article_related_sources_article
        FOREIGN KEY (article_id) REFERENCES news_articles (id) ON DELETE CASCADE
);

CREATE INDEX idx_news_articles_impact ON news_articles (impact);
CREATE INDEX idx_news_articles_collection_status ON news_articles (collection_status);
CREATE INDEX idx_news_article_keywords_keyword ON news_article_keywords (keyword);
