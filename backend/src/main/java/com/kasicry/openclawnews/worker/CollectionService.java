package com.kasicry.openclawnews.worker;

import com.kasicry.openclawnews.news.NewsArticle;
import com.kasicry.openclawnews.news.NewsArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CollectionService {

    private final WorkerClient workerClient;
    private final NewsArticleRepository repository;

    public CollectionService(WorkerClient workerClient, NewsArticleRepository repository) {
        this.workerClient = workerClient;
        this.repository = repository;
    }

    @Transactional
    public WorkerCollectResponse collectAndSave(WorkerCollectRequest request) {
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
                    article.getContent()
            ));
        }

        repository.saveAll(articles);
        return response;
    }
}
