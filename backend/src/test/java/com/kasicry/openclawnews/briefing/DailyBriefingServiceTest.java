package com.kasicry.openclawnews.briefing;

import com.kasicry.openclawnews.news.NewsArticle;
import com.kasicry.openclawnews.news.NewsArticleRepository;
import com.kasicry.openclawnews.operations.OperationalMetrics;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DailyBriefingServiceTest {

    @Test
    void marksArticlesSentOnlyAfterSuccessfulDelivery() {
        NewsArticleRepository repository = mock(NewsArticleRepository.class);
        TelegramSendService telegramSendService = mock(TelegramSendService.class);
        OperationalMetrics metrics = mock(OperationalMetrics.class);
        NewsArticle article = new NewsArticle(
                "openai",
                "Codex release",
                "https://example.com/codex",
                Instant.parse("2026-06-12T00:00:00Z"),
                "Release details"
        );
        when(repository.findByPublishedAtBetweenAndNotificationSentFalseOrderByPublishedAtDesc(
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(Collections.singletonList(article));
        when(telegramSendService.send(any(String.class))).thenReturn(2);

        DailyBriefingService service = new DailyBriefingService(
                repository,
                telegramSendService,
                metrics,
                "Asia/Seoul"
        );
        int sentCount = service.sendToday();

        assertThat(sentCount).isEqualTo(1);
        assertThat(article.isNotificationSent()).isTrue();
        verify(telegramSendService).send(any(String.class));
        verify(repository).saveAll(Collections.singletonList(article));
        verify(metrics).recordBriefing(
                org.mockito.ArgumentMatchers.eq("success"),
                org.mockito.ArgumentMatchers.eq(1),
                org.mockito.ArgumentMatchers.eq(2),
                anyLong()
        );
    }

    @Test
    void skipsDeliveryWhenThereAreNoUnsentArticles() {
        NewsArticleRepository repository = mock(NewsArticleRepository.class);
        TelegramSendService telegramSendService = mock(TelegramSendService.class);
        OperationalMetrics metrics = mock(OperationalMetrics.class);
        when(repository.findByPublishedAtBetweenAndNotificationSentFalseOrderByPublishedAtDesc(
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(Collections.emptyList());

        DailyBriefingService service = new DailyBriefingService(
                repository,
                telegramSendService,
                metrics,
                "Asia/Seoul"
        );

        assertThat(service.sendToday()).isZero();
        verify(telegramSendService, never()).send(any(String.class));
        verify(metrics).recordBriefing(
                org.mockito.ArgumentMatchers.eq("skipped"),
                org.mockito.ArgumentMatchers.eq(0),
                org.mockito.ArgumentMatchers.eq(0),
                anyLong()
        );
    }

    @Test
    void doesNotMarkArticlesSentWhenDeliveryFails() {
        NewsArticleRepository repository = mock(NewsArticleRepository.class);
        TelegramSendService telegramSendService = mock(TelegramSendService.class);
        OperationalMetrics metrics = mock(OperationalMetrics.class);
        NewsArticle article = new NewsArticle(
                "openai",
                "Codex release",
                "https://example.com/codex",
                Instant.parse("2026-06-12T00:00:00Z"),
                "Release details"
        );
        when(repository.findByPublishedAtBetweenAndNotificationSentFalseOrderByPublishedAtDesc(
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(Collections.singletonList(article));
        doThrow(new IllegalStateException("delivery failed"))
                .when(telegramSendService)
                .send(any(String.class));

        DailyBriefingService service = new DailyBriefingService(
                repository,
                telegramSendService,
                metrics,
                "Asia/Seoul"
        );

        org.assertj.core.api.Assertions.assertThatThrownBy(service::sendToday)
                .isInstanceOf(IllegalStateException.class);
        assertThat(article.isNotificationSent()).isFalse();
        verify(repository, never()).saveAll(any());
        verify(metrics).recordBriefing(
                org.mockito.ArgumentMatchers.eq("failure"),
                org.mockito.ArgumentMatchers.eq(0),
                org.mockito.ArgumentMatchers.eq(0),
                anyLong()
        );
    }

    @Test
    void previewsUnsentArticlesWithoutDeliveryOrStateChange() {
        NewsArticleRepository repository = mock(NewsArticleRepository.class);
        TelegramSendService telegramSendService = mock(TelegramSendService.class);
        OperationalMetrics metrics = mock(OperationalMetrics.class);
        NewsArticle article = new NewsArticle(
                "openai",
                "Codex release",
                "https://example.com/codex",
                Instant.parse("2026-06-12T00:00:00Z"),
                "Release details"
        );
        when(repository.findByPublishedAtBetweenAndNotificationSentFalseOrderByPublishedAtDesc(
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(Collections.singletonList(article));
        DailyBriefingService service = new DailyBriefingService(
                repository,
                telegramSendService,
                metrics,
                "Asia/Seoul"
        );

        BriefingPreview preview = service.previewToday();

        assertThat(preview.getArticleCount()).isEqualTo(1);
        assertThat(preview.getText()).contains("Codex release", "https://example.com/codex");
        assertThat(article.isNotificationSent()).isFalse();
        verify(telegramSendService, never()).send(any(String.class));
        verify(repository, never()).saveAll(any());
    }
}
