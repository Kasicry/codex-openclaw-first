package com.kasicry.openclawnews.news;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "news_articles")
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String source;

    @Column(nullable = false, length = 1000)
    private String title;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected NewsArticle() {
    }

    public NewsArticle(String source, String title, String url, Instant publishedAt, String content) {
        this.source = source;
        this.title = title;
        this.url = url;
        this.publishedAt = publishedAt;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
