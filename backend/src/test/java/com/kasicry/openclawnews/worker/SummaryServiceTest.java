package com.kasicry.openclawnews.worker;

import com.kasicry.openclawnews.config.InvalidRequestException;
import com.kasicry.openclawnews.news.CollectionStatus;
import com.kasicry.openclawnews.news.NewsArticle;
import com.kasicry.openclawnews.news.NewsArticleRepository;
import com.kasicry.openclawnews.news.NewsImpact;
import com.kasicry.openclawnews.operations.AlertNotificationService;
import com.kasicry.openclawnews.operations.OperationalAlertEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class SummaryServiceTest {

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private NewsArticleRepository repository;

    @MockBean
    private WorkerClient workerClient;

    @MockBean
    private AlertNotificationService alerts;

    @Test
    void savesStructuredSummary() {
        NewsArticle article = repository.save(new NewsArticle(
                "openai",
                "Codex release",
                "https://example.com/codex",
                Instant.parse("2026-06-11T00:00:00Z"),
                "Release details"
        ));
        WorkerSummaryResponse response = new WorkerSummaryResponse();
        response.setTitle("Codex release");
        response.setCoreContent("Core summary");
        response.setImpact("high");
        response.setDeveloperView("Review the new workflow");
        when(workerClient.summarize(any(WorkerSummaryRequest.class))).thenReturn(response);

        NewsArticle summarized = summaryService.summarize(article.getId());

        assertThat(summarized.getSummary()).isEqualTo("Core summary");
        assertThat(summarized.getImpact()).isEqualTo(NewsImpact.HIGH);
        assertThat(summarized.getDeveloperView()).isEqualTo("Review the new workflow");
        assertThat(summarized.getCollectionStatus()).isEqualTo(CollectionStatus.SUMMARIZED);
    }

    @Test
    void rejectsArticleWithoutContentBeforeCallingWorker() {
        NewsArticle article = repository.save(new NewsArticle(
                "openai",
                "Codex release",
                "https://example.com/codex-without-content",
                Instant.parse("2026-06-11T00:00:00Z"),
                " "
        ));

        assertThatThrownBy(() -> summaryService.summarize(article.getId()))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Article content is required for summary");
        verifyNoInteractions(workerClient);
    }

    @Test
    void alertsWhenSummaryFails() {
        NewsArticle article = repository.save(new NewsArticle(
                "openai",
                "Codex failure",
                "https://example.com/codex-failure",
                Instant.parse("2026-06-11T00:00:00Z"),
                "Release details"
        ));
        when(workerClient.summarize(any(WorkerSummaryRequest.class)))
                .thenThrow(new IllegalStateException("worker failed"));

        assertThatThrownBy(() -> summaryService.summarize(article.getId()))
                .isInstanceOf(IllegalStateException.class);

        verify(alerts).notify(OperationalAlertEvent.SUMMARY_FAILURE);
        assertThat(repository.findById(article.getId()).get().getCollectionStatus())
                .isEqualTo(CollectionStatus.SUMMARY_FAILED);
    }
}
