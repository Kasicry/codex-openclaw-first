package com.kasicry.openclawnews.worker;

import com.kasicry.openclawnews.config.InvalidRequestException;
import com.kasicry.openclawnews.news.NewsArticle;
import com.kasicry.openclawnews.news.NewsArticleRepository;
import com.kasicry.openclawnews.news.NewsImpact;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class SummaryService {

    private final WorkerClient workerClient;
    private final NewsArticleRepository repository;

    public SummaryService(WorkerClient workerClient, NewsArticleRepository repository) {
        this.workerClient = workerClient;
        this.repository = repository;
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

        try {
            WorkerSummaryResponse response = workerClient.summarize(
                    new WorkerSummaryRequest(article.getTitle(), article.getContent())
            );
            article.applySummary(
                    response.getCoreContent(),
                    NewsImpact.valueOf(response.getImpact().toUpperCase(Locale.ROOT)),
                    response.getDeveloperView()
            );
            return repository.save(article);
        } catch (RuntimeException exception) {
            article.markSummaryFailed();
            repository.save(article);
            throw exception;
        }
    }
}
