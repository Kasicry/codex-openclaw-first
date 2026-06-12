package com.kasicry.openclawnews.worker;

import com.kasicry.openclawnews.news.NewsArticle;
import com.kasicry.openclawnews.news.NewsArticleRepository;
import com.kasicry.openclawnews.operations.AlertNotificationService;
import com.kasicry.openclawnews.operations.OperationalAlertEvent;
import com.kasicry.openclawnews.operations.OperationalMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class CollectionService {

    private final WorkerClient workerClient;
    private final NewsArticleRepository repository;
    private final OperationalMetrics metrics;
    private final AlertNotificationService alerts;

    public CollectionService(
            WorkerClient workerClient,
            NewsArticleRepository repository,
            OperationalMetrics metrics,
            AlertNotificationService alerts
    ) {
        this.workerClient = workerClient;
        this.repository = repository;
        this.metrics = metrics;
        this.alerts = alerts;
    }

    @Transactional
    public WorkerCollectResponse collectAndSave(WorkerCollectRequest request) {
        long startedAt = System.nanoTime();
        try {
            WorkerCollectResponse response = workerClient.collect(request);
            List<NewsArticle> articles = new ArrayList<NewsArticle>();

            for (WorkerCollectResponse.Article article : response.getArticles()) {
                if (repository.existsByUrl(article.getUrl())) {
                    continue;
                }
                articles.add(new NewsArticle(
                        article.getSource(),
                        article.getTitle(),
                        article.getUrl(),
                        article.getPublishedAt(),
                        article.getContent(),
                        article.getMatchedKeywords(),
                        article.getRelatedSources()
                ));
            }

            repository.saveAll(articles);
            repository.flush();
            metrics.recordCollectionSuccess(response, articles.size(), elapsedSince(startedAt));
            if (hasUnsuccessfulSource(response)) {
                alerts.notify(OperationalAlertEvent.COLLECTION_PARTIAL);
            }
            return response;
        } catch (RuntimeException exception) {
            metrics.recordCollectionFailure(elapsedSince(startedAt));
            alerts.notify(OperationalAlertEvent.COLLECTION_FAILURE);
            throw exception;
        }
    }

    private boolean hasUnsuccessfulSource(WorkerCollectResponse response) {
        for (WorkerCollectResponse.SourceResult source : response.getSources()) {
            String status = source.getStatus() == null
                    ? ""
                    : source.getStatus().trim().toLowerCase(Locale.ROOT);
            if (!"success".equals(status) && !"completed".equals(status) && !"ok".equals(status)) {
                return true;
            }
        }
        return false;
    }

    private long elapsedSince(long startedAt) {
        return System.nanoTime() - startedAt;
    }
}
