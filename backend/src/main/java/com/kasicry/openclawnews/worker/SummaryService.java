package com.kasicry.openclawnews.worker;

import com.kasicry.openclawnews.config.InvalidRequestException;
import com.kasicry.openclawnews.news.NewsArticle;
import com.kasicry.openclawnews.news.NewsArticleRepository;
import com.kasicry.openclawnews.news.NewsImpact;
import com.kasicry.openclawnews.operations.OperationalMetrics;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class SummaryService {

    private final WorkerClient workerClient;
    private final NewsArticleRepository repository;
    private final OperationalMetrics metrics;

    public SummaryService(
            WorkerClient workerClient,
            NewsArticleRepository repository,
            OperationalMetrics metrics
    ) {
        this.workerClient = workerClient;
        this.repository = repository;
        this.metrics = metrics;
    }

    public NewsArticle summarize(long articleId) {
        NewsArticle article = repository.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "News article not found"
                ));
        if (article.getContent() == null || article.getContent().trim().isEmpty()) {
            throw new InvalidRequestException("Article content is required for summary");
        }

        long startedAt = System.nanoTime();
        try {
            WorkerSummaryResponse response = workerClient.summarize(
                    new WorkerSummaryRequest(article.getTitle(), article.getContent())
            );
            article.applySummary(
                    response.getCoreContent(),
                    NewsImpact.valueOf(response.getImpact().toUpperCase(Locale.ROOT)),
                    response.getDeveloperView()
            );
            NewsArticle saved = repository.save(article);
            metrics.recordSummary("success", elapsedSince(startedAt));
            return saved;
        } catch (RuntimeException exception) {
            article.markSummaryFailed();
            repository.save(article);
            metrics.recordSummary("failure", elapsedSince(startedAt));
            throw exception;
        }
    }

    private long elapsedSince(long startedAt) {
        return System.nanoTime() - startedAt;
    }
}
