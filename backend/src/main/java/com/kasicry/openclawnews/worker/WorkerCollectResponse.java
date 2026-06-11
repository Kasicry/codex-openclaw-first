package com.kasicry.openclawnews.worker;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WorkerCollectResponse {

    private List<Article> articles = new ArrayList<Article>();
    private List<SourceResult> sources = new ArrayList<SourceResult>();
    private int duplicateCount;
    private int durationMs;

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public List<SourceResult> getSources() {
        return sources;
    }

    public void setSources(List<SourceResult> sources) {
        this.sources = sources;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(int durationMs) {
        this.durationMs = durationMs;
    }

    public static class Article {
        private String source;
        private String title;
        private String url;
        private Instant publishedAt;
        private String content;
        private List<String> matchedKeywords = new ArrayList<String>();
        private List<String> relatedSources = new ArrayList<String>();

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Instant getPublishedAt() {
            return publishedAt;
        }

        public void setPublishedAt(Instant publishedAt) {
            this.publishedAt = publishedAt;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public void setMatchedKeywords(List<String> matchedKeywords) {
            this.matchedKeywords = matchedKeywords;
        }

        public List<String> getRelatedSources() {
            return relatedSources;
        }

        public void setRelatedSources(List<String> relatedSources) {
            this.relatedSources = relatedSources;
        }
    }

    public static class SourceResult {
        private String source;
        private String status;
        private int articleCount;
        private int durationMs;
        private String error;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getArticleCount() {
            return articleCount;
        }

        public void setArticleCount(int articleCount) {
            this.articleCount = articleCount;
        }

        public int getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(int durationMs) {
            this.durationMs = durationMs;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
