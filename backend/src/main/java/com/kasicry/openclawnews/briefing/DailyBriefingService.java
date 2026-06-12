package com.kasicry.openclawnews.briefing;

import com.kasicry.openclawnews.news.NewsArticle;
import com.kasicry.openclawnews.news.NewsArticleRepository;
import com.kasicry.openclawnews.operations.AlertNotificationService;
import com.kasicry.openclawnews.operations.OperationalAlertEvent;
import com.kasicry.openclawnews.operations.OperationalMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class DailyBriefingService {

    private final NewsArticleRepository repository;
    private final TelegramSendService telegramSendService;
    private final OperationalMetrics metrics;
    private final AlertNotificationService alerts;
    private final ZoneId briefingZone;

    public DailyBriefingService(
            NewsArticleRepository repository,
            TelegramSendService telegramSendService,
            OperationalMetrics metrics,
            AlertNotificationService alerts,
            @Value("${operations.briefing-zone:Asia/Seoul}") String briefingZone
    ) {
        this.repository = repository;
        this.telegramSendService = telegramSendService;
        this.metrics = metrics;
        this.alerts = alerts;
        this.briefingZone = ZoneId.of(briefingZone);
    }

    public int sendToday() {
        long startedAt = System.nanoTime();
        LocalDate today = LocalDate.now(briefingZone);
        List<NewsArticle> articles = findUnsentArticles(today);
        if (articles.isEmpty()) {
            metrics.recordBriefing("skipped", 0, 0, elapsedSince(startedAt));
            return 0;
        }

        try {
            int chunkCount = telegramSendService.send(format(today, articles));
            for (NewsArticle article : articles) {
                article.markNotificationSent();
            }
            repository.saveAll(articles);
            metrics.recordBriefing(
                    "success",
                    articles.size(),
                    chunkCount,
                    elapsedSince(startedAt)
            );
            return articles.size();
        } catch (RuntimeException exception) {
            metrics.recordBriefing("failure", 0, 0, elapsedSince(startedAt));
            alerts.notify(OperationalAlertEvent.BRIEFING_FAILURE);
            throw exception;
        }
    }

    public BriefingPreview previewToday() {
        LocalDate today = LocalDate.now(briefingZone);
        List<NewsArticle> articles = findUnsentArticles(today);
        return new BriefingPreview(today, articles.size(), format(today, articles));
    }

    private long elapsedSince(long startedAt) {
        return System.nanoTime() - startedAt;
    }

    private List<NewsArticle> findUnsentArticles(LocalDate date) {
        Instant from = date.atStartOfDay(briefingZone).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(briefingZone).toInstant();
        return repository.findByPublishedAtBetweenAndNotificationSentFalseOrderByPublishedAtDesc(
                from,
                to
        );
    }

    private String format(LocalDate date, List<NewsArticle> articles) {
        StringBuilder briefing = new StringBuilder();
        briefing.append("IT News Briefing ").append(date).append("\n\n");
        for (NewsArticle article : articles) {
            briefing.append("- ").append(article.getTitle()).append("\n");
            if (article.getSummary() != null && !article.getSummary().trim().isEmpty()) {
                briefing.append(article.getSummary()).append("\n");
            }
            briefing.append(article.getUrl()).append("\n\n");
        }
        return briefing.toString().trim();
    }
}
