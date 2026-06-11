package com.kasicry.openclawnews.briefing;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TelegramSendService {

    static final int MAX_MESSAGE_LENGTH = 4000;

    private final TelegramClient telegramClient;

    public TelegramSendService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public int send(String text) {
        List<String> chunks = split(text);
        for (String chunk : chunks) {
            telegramClient.send(chunk);
        }
        return chunks.size();
    }

    List<String> split(String text) {
        List<String> chunks = new ArrayList<String>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + MAX_MESSAGE_LENGTH, text.length());
            if (end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))) {
                end--;
            }
            chunks.add(text.substring(start, end));
            start = end;
        }
        return chunks;
    }
}
