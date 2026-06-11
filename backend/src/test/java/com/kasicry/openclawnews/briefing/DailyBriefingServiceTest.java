package com.kasicry.openclawnews.briefing;

import com.kasicry.openclawnews.news.NewsArticle;
import com.kasicry.openclawnews.news.NewsArticleRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
                "Asia/Seoul"
        );
        int sentCount = service.sendToday();

        assertThat(sentCount).isEqualTo(1);
        assertThat(article.isNotificationSent()).isTrue();
        verify(telegramSendService).send(any(String.class));
        verify(repository).saveAll(Collections.singletonList(article));
    }

    @Test
    void skipsDeliveryWhenThereAreNoUnsentArticles() {
        NewsArticleRepository repository = mock(NewsArticleRepository.class);
        TelegramSendService telegramSendService = mock(TelegramSendService.class);
        when(repository.findByPublishedAtBetweenAndNotificationSentFalseOrderByPublishedAtDesc(
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(Collections.emptyList());

        DailyBriefingService service = new DailyBriefingService(
                repository,
                telegramSendService,
                "Asia/Seoul"
        );

        assertThat(service.sendToday()).isZero();
        verify(telegramSendService, never()).send(any(String.class));
    }

    @Test
    void doesNotMarkArticlesSentWhenDeliveryFails() {
        NewsArticleRepository repository = mock(NewsArticleRepository.class);
        TelegramSendService telegramSendService = mock(TelegramSendService.class);
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
                "Asia/Seoul"
        );

        org.assertj.core.api.Assertions.assertThatThrownBy(service::sendToday)
                .isInstanceOf(IllegalStateException.class);
        assertThat(article.isNotificationSent()).isFalse();
        verify(repository, never()).saveAll(any());
    }
}
