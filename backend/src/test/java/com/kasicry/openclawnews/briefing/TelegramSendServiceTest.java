package com.kasicry.openclawnews.briefing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TelegramSendServiceTest {

    @Test
    void splitsLongMessagesWithoutBreakingSurrogatePairs() {
        TelegramClient telegramClient = mock(TelegramClient.class);
        TelegramSendService service = new TelegramSendService(telegramClient);
        String text = repeat("a", 3999) + "\uD83D\uDE80" + repeat("b", 4001);

        List<String> chunks = service.split(text);
        int chunkCount = service.send(text);

        assertThat(chunks).hasSize(3);
        assertThat(chunks).allMatch(chunk -> chunk.length() <= TelegramSendService.MAX_MESSAGE_LENGTH);
        assertThat(String.join("", chunks)).isEqualTo(text);
        assertThat(chunkCount).isEqualTo(3);
        verify(telegramClient, times(3)).send(org.mockito.ArgumentMatchers.anyString());
    }

    private String repeat(String value, int count) {
        StringBuilder result = new StringBuilder(count);
        for (int index = 0; index < count; index++) {
            result.append(value);
        }
        return result.toString();
    }
}
