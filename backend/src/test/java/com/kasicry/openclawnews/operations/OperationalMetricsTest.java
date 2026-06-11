package com.kasicry.openclawnews.operations;

import com.kasicry.openclawnews.worker.WorkerCollectResponse;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class OperationalMetricsTest {

    @Test
    void recordsCollectionOutcomesAndBoundsSourceTags() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OperationalMetrics metrics = new OperationalMetrics(registry);
        WorkerCollectResponse response = new WorkerCollectResponse();
        response.setArticles(Arrays.asList(
                new WorkerCollectResponse.Article(),
                new WorkerCollectResponse.Article()
        ));
        response.setDuplicateCount(1);
        response.setSources(Arrays.asList(
                source("openai", "completed"),
                source("untrusted-dynamic-source", "failed")
        ));

        metrics.recordCollectionSuccess(response, 1, 10_000_000);

        assertThat(counter(registry, "openclaw.collection.runs", "result", "partial"))
                .isEqualTo(1);
        assertThat(counter(registry, "openclaw.collection.articles", "result", "received"))
                .isEqualTo(2);
        assertThat(counter(registry, "openclaw.collection.articles", "result", "saved"))
                .isEqualTo(1);
        assertThat(counter(registry, "openclaw.collection.articles", "result", "existing"))
                .isEqualTo(1);
        assertThat(counter(registry, "openclaw.collection.articles", "result", "duplicate"))
                .isEqualTo(1);
        assertThat(counter(
                registry,
                "openclaw.collection.sources",
                "source",
                "other",
                "result",
                "failure"
        )).isEqualTo(1);
        assertThat(registry.find("openclaw.collection.duration").timer().count()).isEqualTo(1);
    }

    @Test
    void recordsFailuresSummaryAndBriefingResults() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OperationalMetrics metrics = new OperationalMetrics(registry);

        metrics.recordCollectionFailure(1);
        metrics.recordSummary("success", 1);
        metrics.recordSummary("failure", 1);
        metrics.recordBriefing("success", 3, 2, 1);
        metrics.recordBriefing("skipped", 0, 0, 1);
        metrics.recordBriefing("failure", 0, 0, 1);

        assertThat(counter(registry, "openclaw.collection.runs", "result", "failure"))
                .isEqualTo(1);
        assertThat(counter(registry, "openclaw.summary.runs", "result", "success")).isEqualTo(1);
        assertThat(counter(registry, "openclaw.summary.runs", "result", "failure")).isEqualTo(1);
        assertThat(counter(registry, "openclaw.briefing.runs", "result", "success")).isEqualTo(1);
        assertThat(counter(registry, "openclaw.briefing.runs", "result", "skipped")).isEqualTo(1);
        assertThat(counter(registry, "openclaw.briefing.runs", "result", "failure")).isEqualTo(1);
        assertThat(counter(registry, "openclaw.briefing.articles", "result", "sent"))
                .isEqualTo(3);
        assertThat(counter(registry, "openclaw.briefing.chunks", "result", "sent")).isEqualTo(2);
    }

    private WorkerCollectResponse.SourceResult source(String source, String status) {
        WorkerCollectResponse.SourceResult result = new WorkerCollectResponse.SourceResult();
        result.setSource(source);
        result.setStatus(status);
        return result;
    }

    private double counter(
            SimpleMeterRegistry registry,
            String name,
            String tagName,
            String tagValue
    ) {
        return registry.find(name).tag(tagName, tagValue).counter().count();
    }

    private double counter(
            SimpleMeterRegistry registry,
            String name,
            String firstTagName,
            String firstTagValue,
            String secondTagName,
            String secondTagValue
    ) {
        return registry.find(name)
                .tags(firstTagName, firstTagValue, secondTagName, secondTagValue)
                .counter()
                .count();
    }
}
