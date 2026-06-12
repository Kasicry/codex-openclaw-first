package com.kasicry.openclawnews.operations;

import com.kasicry.openclawnews.worker.WorkerCollectResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class OperationalMetrics {

    private static final Set<String> KNOWN_SOURCES = new HashSet<String>(Arrays.asList(
            "openai",
            "anthropic",
            "google-ai"
    ));

    private final MeterRegistry registry;

    public OperationalMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordCollectionSuccess(
            WorkerCollectResponse response,
            int savedArticleCount,
            long durationNanos
    ) {
        increment("openclaw.collection.runs", "result", collectionResult(response), 1);
        increment("openclaw.collection.articles", "result", "received", response.getArticles().size());
        increment("openclaw.collection.articles", "result", "saved", savedArticleCount);
        increment(
                "openclaw.collection.articles",
                "result",
                "existing",
                response.getArticles().size() - savedArticleCount
        );
        increment("openclaw.collection.articles", "result", "duplicate", response.getDuplicateCount());
        for (WorkerCollectResponse.SourceResult source : response.getSources()) {
            registry.counter(
                    "openclaw.collection.sources",
                    "source",
                    normalizeSource(source.getSource()),
                    "result",
                    normalizeResult(source.getStatus())
            ).increment();
        }
        recordDuration("openclaw.collection.duration", durationNanos);
    }

    public void recordCollectionFailure(long durationNanos) {
        increment("openclaw.collection.runs", "result", "failure", 1);
        recordDuration("openclaw.collection.duration", durationNanos);
    }

    public void recordSummary(String result, long durationNanos) {
        increment("openclaw.summary.runs", "result", normalizeResult(result), 1);
        recordDuration("openclaw.summary.duration", durationNanos);
    }

    public void recordBriefing(String result, int articleCount, int chunkCount, long durationNanos) {
        String normalizedResult = normalizeResult(result);
        increment("openclaw.briefing.runs", "result", normalizedResult, 1);
        if ("success".equals(normalizedResult)) {
            increment("openclaw.briefing.articles", "result", "sent", articleCount);
            increment("openclaw.briefing.chunks", "result", "sent", chunkCount);
        }
        recordDuration("openclaw.briefing.duration", durationNanos);
    }

    public void recordAlertDelivery(String result) {
        increment("openclaw.alert.deliveries", "result", normalizeResult(result), 1);
    }

    private String collectionResult(WorkerCollectResponse response) {
        for (WorkerCollectResponse.SourceResult source : response.getSources()) {
            if (!"success".equals(normalizeResult(source.getStatus()))) {
                return "partial";
            }
        }
        return "success";
    }

    private String normalizeSource(String source) {
        if (source == null) {
            return "other";
        }
        String normalized = source.trim().toLowerCase(Locale.ROOT);
        return KNOWN_SOURCES.contains(normalized) ? normalized : "other";
    }

    private String normalizeResult(String result) {
        if (result == null) {
            return "unknown";
        }
        String normalized = result.trim().toLowerCase(Locale.ROOT);
        if ("ok".equals(normalized) || "completed".equals(normalized)) {
            return "success";
        }
        if ("error".equals(normalized) || "failed".equals(normalized)) {
            return "failure";
        }
        switch (normalized) {
            case "success":
            case "failure":
            case "partial":
            case "skipped":
                return normalized;
            default:
                return "unknown";
        }
    }

    private void increment(String name, String tagName, String tagValue, double amount) {
        if (amount > 0) {
            registry.counter(name, tagName, tagValue).increment(amount);
        }
    }

    private void recordDuration(String name, long durationNanos) {
        Timer.builder(name)
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofMinutes(5))
                .register(registry)
                .record(Math.max(0, durationNanos), TimeUnit.NANOSECONDS);
    }
}
