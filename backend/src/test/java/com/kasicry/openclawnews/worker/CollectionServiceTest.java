package com.kasicry.openclawnews.worker;

import com.kasicry.openclawnews.news.NewsArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class CollectionServiceTest {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private NewsArticleRepository repository;

    @MockBean
    private WorkerClient workerClient;

    @Test
    void savesWorkerArticles() {
        WorkerCollectResponse.Article article = new WorkerCollectResponse.Article();
        article.setSource("openai");
        article.setTitle("Codex release");
        article.setUrl("https://openai.com/index/codex-release/");
        article.setPublishedAt(Instant.parse("2026-06-11T00:00:00Z"));
        article.setContent("Release details");

        WorkerCollectResponse response = new WorkerCollectResponse();
        response.setArticles(Collections.singletonList(article));
        when(workerClient.collect(any(WorkerCollectRequest.class))).thenReturn(response);

        collectionService.collectAndSave(new WorkerCollectRequest());
        collectionService.collectAndSave(new WorkerCollectRequest());

        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findAll().get(0).getTitle()).isEqualTo("Codex release");
    }
}
