package com.kasicry.openclawnews.briefing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BriefingController.class)
class BriefingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TelegramSendService telegramSendService;

    @Test
    void telegramSendIsForbiddenByDefault() throws Exception {
        mockMvc.perform(post("/api/briefing/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Daily briefing\"}"))
                .andExpect(status().isForbidden());
    }
}
