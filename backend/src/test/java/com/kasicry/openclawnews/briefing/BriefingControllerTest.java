package com.kasicry.openclawnews.briefing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BriefingController.class)
class BriefingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TelegramSendService telegramSendService;

    @MockBean
    private DailyBriefingService dailyBriefingService;

    @Test
    void telegramSendIsForbiddenByDefault() throws Exception {
        mockMvc.perform(post("/api/briefing/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Daily briefing\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void previewsTodayWithoutSending() throws Exception {
        when(dailyBriefingService.previewToday()).thenReturn(new BriefingPreview(
                LocalDate.parse("2026-06-12"),
                2,
                "IT News Briefing"
        ));

        mockMvc.perform(get("/api/briefing/preview/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-06-12"))
                .andExpect(jsonPath("$.article_count").value(2))
                .andExpect(jsonPath("$.text").value("IT News Briefing"));
    }

    @Test
    void generatedBriefingSendIsForbiddenByDefault() throws Exception {
        mockMvc.perform(post("/api/briefing/send/today"))
                .andExpect(status().isForbidden());
    }

    @Test
    void generatedBriefingSendReturnsArticleCountWhenEnabled() {
        TelegramSendService telegram = mock(TelegramSendService.class);
        DailyBriefingService daily = mock(DailyBriefingService.class);
        when(daily.sendToday()).thenReturn(3);
        BriefingController controller = new BriefingController(telegram, daily, true);

        BriefingSendResult result = controller.sendToday();

        assertThat(result.getSentArticleCount()).isEqualTo(3);
        verify(daily).sendToday();
    }
}
