package com.kasicry.openclawnews.briefing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramClient {

    private final RestTemplate restTemplate;
    private final String botToken;
    private final String chatId;
    private final int maxAttempts;

    public TelegramClient(
            RestTemplate restTemplate,
            @Value("${telegram.bot-token:}") String botToken,
            @Value("${telegram.chat-id:}") String chatId,
            @Value("${telegram.max-attempts:3}") int maxAttempts
    ) {
        this.restTemplate = restTemplate;
        this.botToken = botToken;
        this.chatId = chatId;
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    public void send(String text) {
        if (botToken.isEmpty() || chatId.isEmpty()) {
            throw new IllegalStateException("Telegram configuration is incomplete");
        }

        Map<String, String> payload = new HashMap<String, String>();
        payload.put("chat_id", chatId);
        payload.put("text", text);
        RestClientException lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                restTemplate.postForObject(
                        "https://api.telegram.org/bot" + botToken + "/sendMessage",
                        payload,
                        String.class
                );
                return;
            } catch (RestClientException exception) {
                lastException = exception;
            }
        }
        throw new IllegalStateException("Telegram send failed after retries", lastException);
    }
}
