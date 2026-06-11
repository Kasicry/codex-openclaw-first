package com.kasicry.openclawnews.worker;

import com.kasicry.openclawnews.news.NewsArticle;
import com.kasicry.openclawnews.news.NewsArticleRepository;
import com.kasicry.openclawnews.operations.OperationalMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CollectionService {

    private final WorkerClient workerClient;
    private final NewsArticleRepository repository;
    private final OperationalMetrics metrics;

    public CollectionService(
            WorkerClient workerClient,
            NewsArticleRepository repository,
            OperationalMetrics metrics
    ) {
        this.workerClient = workerClient;
        this.repository = repository;
        this.metrics = metrics;
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
            return response;
        } catch (RuntimeException exception) {
            metrics.recordCollectionFailure(elapsedSince(startedAt));
            throw exception;
        }
    }

    private long elapsedSince(long startedAt) {
        return System.nanoTime() - startedAt;
    }
}
