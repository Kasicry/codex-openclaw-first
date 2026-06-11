package com.kasicry.openclawnews.briefing;

import com.kasicry.openclawnews.news.NewsArticle;
import com.kasicry.openclawnews.news.NewsArticleRepository;
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
    private final ZoneId briefingZone;

    public DailyBriefingService(
            NewsArticleRepository repository,
            TelegramSendService telegramSendService,
            @Value("${operations.briefing-zone:Asia/Seoul}") String briefingZone
    ) {
        this.repository = repository;
        this.telegramSendService = telegramSendService;
        this.briefingZone = ZoneId.of(briefingZone);
    }

    public int sendToday() {
        LocalDate today = LocalDate.now(briefingZone);
        Instant from = today.atStartOfDay(briefingZone).toInstant();
        Instant to = today.plusDays(1).atStartOfDay(briefingZone).toInstant();
        List<NewsArticle> articles =
                repository.findByPublishedAtBetweenAndNotificationSentFalseOrderByPublishedAtDesc(
                        from,
                        to
                );
        if (articles.isEmpty()) {
            return 0;
        }

        telegramSendService.send(format(today, articles));
        for (NewsArticle article : articles) {
            article.markNotificationSent();
        }
        repository.saveAll(articles);
        return articles.size();
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
