package com.kasicry.openclawnews.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ReadApiProtectionFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final long WINDOW_MILLIS = 60_000L;

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final boolean enabled;
    private final String configuredApiKey;
    private final int requestsPerMinute;
    private final AtomicInteger requestCount = new AtomicInteger();
    private long windowStartedAt;

    @Autowired
    public ReadApiProtectionFilter(
            ObjectMapper objectMapper,
            @Value("${api.read-protection-enabled:false}") boolean enabled,
            @Value("${api.read-key:}") String configuredApiKey,
            @Value("${api.requests-per-minute:60}") int requestsPerMinute
    ) {
        this(objectMapper, Clock.systemUTC(), enabled, configuredApiKey, requestsPerMinute);
    }

    ReadApiProtectionFilter(
            ObjectMapper objectMapper,
            Clock clock,
            boolean enabled,
            String configuredApiKey,
            int requestsPerMinute
    ) {
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.enabled = enabled;
        this.configuredApiKey = configuredApiKey;
        this.requestsPerMinute = Math.max(1, requestsPerMinute);
        this.windowStartedAt = clock.millis();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return !enabled
                || !"GET".equalsIgnoreCase(request.getMethod())
                || !(requestUri.equals("/api/news") || requestUri.startsWith("/api/news/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!hasValidApiKey(request.getHeader(API_KEY_HEADER))) {
            writeError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Valid API key required");
            return;
        }
        if (exceedsRateLimit()) {
            writeError(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "RATE_LIMIT_EXCEEDED",
                    "Read API rate limit exceeded"
            );
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean hasValidApiKey(String suppliedApiKey) {
        if (configuredApiKey.isEmpty() || suppliedApiKey == null) {
            return false;
        }
        return MessageDigest.isEqual(
                configuredApiKey.getBytes(StandardCharsets.UTF_8),
                suppliedApiKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    private synchronized boolean exceedsRateLimit() {
        long now = clock.millis();
        if (now - windowStartedAt >= WINDOW_MILLIS) {
            windowStartedAt = now;
            requestCount.set(0);
        }
        return requestCount.incrementAndGet() > requestsPerMinute;
    }

    private void writeError(
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiError(code, message));
    }
}
