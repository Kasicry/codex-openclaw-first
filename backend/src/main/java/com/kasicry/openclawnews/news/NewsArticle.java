package com.kasicry.openclawnews.news;

import javax.persistence.Column;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NewsImpact impact = NewsImpact.PENDING;

    @Column(name = "developer_view", columnDefinition = "TEXT")
    private String developerView;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_status", nullable = false, length = 32)
    private CollectionStatus collectionStatus = CollectionStatus.COLLECTED;

    @Column(name = "notification_sent", nullable = false)
    private boolean notificationSent;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "news_article_keywords",
            joinColumns = @JoinColumn(name = "article_id")
    )
    @Column(name = "keyword", nullable = false, length = 120)
    private Set<String> keywords = new LinkedHashSet<String>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "news_article_related_sources",
            joinColumns = @JoinColumn(name = "article_id")
    )
    @Column(name = "source", nullable = false, length = 120)
    private Set<String> relatedSources = new LinkedHashSet<String>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected NewsArticle() {
    }

    public NewsArticle(String source, String title, String url, Instant publishedAt, String content) {
        this(source, title, url, publishedAt, content, null, null);
    }

    public NewsArticle(
            String source,
            String title,
            String url,
            Instant publishedAt,
            String content,
            Collection<String> keywords,
            Collection<String> relatedSources
    ) {
        this.source = source;
        this.title = title;
        this.url = url;
        this.publishedAt = publishedAt;
        this.content = content;
        if (keywords != null) {
            this.keywords.addAll(keywords);
        }
        if (relatedSources != null) {
            this.relatedSources.addAll(relatedSources);
        }
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

    public String getSummary() {
        return summary;
    }

    public NewsImpact getImpact() {
        return impact;
    }

    public String getDeveloperView() {
        return developerView;
    }

    public CollectionStatus getCollectionStatus() {
        return collectionStatus;
    }

    public boolean isNotificationSent() {
        return notificationSent;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public Set<String> getRelatedSources() {
        return relatedSources;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void applySummary(String summary, NewsImpact impact, String developerView) {
        this.summary = summary;
        this.impact = impact;
        this.developerView = developerView;
        this.collectionStatus = CollectionStatus.SUMMARIZED;
    }

    public void markSummaryFailed() {
        this.collectionStatus = CollectionStatus.SUMMARY_FAILED;
    }

    public void markNotificationSent() {
        this.notificationSent = true;
    }
}
