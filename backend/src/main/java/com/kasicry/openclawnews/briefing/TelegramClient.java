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

    public TelegramClient(
            RestTemplate restTemplate,
            @Value("${telegram.bot-token:}") String botToken,
            @Value("${telegram.chat-id:}") String chatId
    ) {
        this.restTemplate = restTemplate;
        this.botToken = botToken;
        this.chatId = chatId;
    }

    public void send(String text) {
        if (botToken.isEmpty() || chatId.isEmpty()) {
            throw new IllegalStateException("Telegram configuration is incomplete");
        }

        Map<String, String> payload = new HashMap<String, String>();
        payload.put("chat_id", chatId);
        payload.put("text", text);
        try {
            restTemplate.postForObject(
                    "https://api.telegram.org/bot" + botToken + "/sendMessage",
                    payload,
                    String.class
            );
        } catch (RestClientException exception) {
            throw new IllegalStateException("Telegram send failed");
        }
    }
}
