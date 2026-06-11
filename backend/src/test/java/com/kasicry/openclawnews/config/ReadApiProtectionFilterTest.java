package com.kasicry.openclawnews.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ReadApiProtectionFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-12T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void rejectsMissingApiKeyWhenEnabled() throws Exception {
        ReadApiProtectionFilter filter = new ReadApiProtectionFilter(
                objectMapper,
                clock,
                true,
                "test-only-key",
                60
        );
        MockHttpServletRequest request = readRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("UNAUTHORIZED");
    }

    @Test
    void limitsAuthorizedReadRequests() throws Exception {
        ReadApiProtectionFilter filter = new ReadApiProtectionFilter(
                objectMapper,
                clock,
                true,
                "test-only-key",
                1
        );

        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(authorizedReadRequest(), firstResponse, new MockFilterChain());
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(authorizedReadRequest(), secondResponse, new MockFilterChain());

        assertThat(firstResponse.getStatus()).isEqualTo(200);
        assertThat(secondResponse.getStatus()).isEqualTo(429);
        assertThat(secondResponse.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
    }

    @Test
    void ignoresWriteOperations() throws Exception {
        ReadApiProtectionFilter filter = new ReadApiProtectionFilter(
                objectMapper,
                clock,
                true,
                "test-only-key",
                1
        );
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/news/collect");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private MockHttpServletRequest authorizedReadRequest() {
        MockHttpServletRequest request = readRequest();
        request.addHeader("X-API-Key", "test-only-key");
        return request;
    }

    private MockHttpServletRequest readRequest() {
        return new MockHttpServletRequest("GET", "/api/news/latest");
    }
}
