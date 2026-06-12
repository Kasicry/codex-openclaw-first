package com.kasicry.openclawnews.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AlertNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertNotificationService.class);

    private final RestTemplate restTemplate;
    private final OperationalMetrics metrics;
    private final boolean enabled;
    private final String webhookUrl;

    public AlertNotificationService(
            @Qualifier("alertRestTemplate") RestTemplate restTemplate,
            OperationalMetrics metrics,
            @Value("${operations.alert-webhook-enabled:false}") boolean enabled,
            @Value("${operations.alert-webhook-url:}") String webhookUrl
    ) {
        this.restTemplate = restTemplate;
        this.metrics = metrics;
        this.enabled = enabled;
        this.webhookUrl = webhookUrl.trim();
    }

    public boolean notify(OperationalAlertEvent event) {
        if (!enabled) {
            return false;
        }
        try {
            if (webhookUrl.isEmpty()) {
                metrics.recordAlertDelivery("failure");
                LOGGER.warn(
                        "Operational alert webhook is enabled but URL is empty event={}",
                        event.getCode()
                );
                return false;
            }

            Map<String, String> payload = new LinkedHashMap<String, String>();
            payload.put("schema_version", "1");
            payload.put("severity", event.getSeverity());
            payload.put("component", event.getComponent());
            payload.put("event", event.getCode());
            payload.put("occurred_at", Instant.now().toString());

            restTemplate.postForEntity(webhookUrl, payload, Void.class);
            metrics.recordAlertDelivery("success");
            return true;
        } catch (RuntimeException exception) {
            recordFailureSafely();
            LOGGER.warn("Operational alert delivery failed event={}", event.getCode());
            return false;
        }
    }

    private void recordFailureSafely() {
        try {
            metrics.recordAlertDelivery("failure");
        } catch (RuntimeException exception) {
            LOGGER.warn("Operational alert failure metric could not be recorded");
        }
    }
}
