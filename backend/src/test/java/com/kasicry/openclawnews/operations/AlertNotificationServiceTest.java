package com.kasicry.openclawnews.operations;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class AlertNotificationServiceTest {

    @Test
    void disabledWebhookDoesNotSend() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AlertNotificationService service = new AlertNotificationService(
                restTemplate,
                new OperationalMetrics(registry),
                false,
                "https://alerts.example.test/hook"
        );

        assertThat(service.notify(OperationalAlertEvent.SUMMARY_FAILURE)).isFalse();
        assertThat(registry.find("openclaw.alert.deliveries").counter()).isNull();
        server.verify();
    }

    @Test
    void enabledWebhookWithoutUrlDoesNotSend() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AlertNotificationService service = new AlertNotificationService(
                restTemplate,
                new OperationalMetrics(registry),
                true,
                " "
        );

        assertThat(service.notify(OperationalAlertEvent.SUMMARY_FAILURE)).isFalse();
        assertThat(registry.find("openclaw.alert.deliveries")
                .tag("result", "failure")
                .counter()
                .count()).isEqualTo(1);
        server.verify();
    }

    @Test
    void sendsOnlyFixedOperationalMetadata() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        server.expect(requestTo("https://alerts.example.test/hook"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.schema_version").value("1"))
                .andExpect(jsonPath("$.severity").value("critical"))
                .andExpect(jsonPath("$.component").value("briefing"))
                .andExpect(jsonPath("$.event").value("briefing.failure"))
                .andExpect(jsonPath("$.occurred_at").exists())
                .andExpect(jsonPath("$.title").doesNotExist())
                .andExpect(jsonPath("$.url").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist())
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        AlertNotificationService service = new AlertNotificationService(
                restTemplate,
                new OperationalMetrics(registry),
                true,
                "https://alerts.example.test/hook"
        );

        assertThat(service.notify(OperationalAlertEvent.BRIEFING_FAILURE)).isTrue();
        assertThat(registry.find("openclaw.alert.deliveries")
                .tag("result", "success")
                .counter()
                .count()).isEqualTo(1);
        server.verify();
    }

    @Test
    void deliveryFailureIsIsolated() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        server.expect(requestTo("https://alerts.example.test/hook"))
                .andRespond(withServerError());
        AlertNotificationService service = new AlertNotificationService(
                restTemplate,
                new OperationalMetrics(registry),
                true,
                "https://alerts.example.test/hook"
        );

        assertThat(service.notify(OperationalAlertEvent.COLLECTION_FAILURE)).isFalse();
        assertThat(registry.find("openclaw.alert.deliveries")
                .tag("result", "failure")
                .counter()
                .count()).isEqualTo(1);
        server.verify();
    }

    @Test
    void metricFailureIsAlsoIsolated() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        OperationalMetrics metrics = mock(OperationalMetrics.class);
        server.expect(requestTo("https://alerts.example.test/hook"))
                .andRespond(withServerError());
        doThrow(new IllegalStateException("metric failed"))
                .when(metrics)
                .recordAlertDelivery("failure");
        AlertNotificationService service = new AlertNotificationService(
                restTemplate,
                metrics,
                true,
                "https://alerts.example.test/hook"
        );

        assertThat(service.notify(OperationalAlertEvent.COLLECTION_FAILURE)).isFalse();
        server.verify();
    }
}
