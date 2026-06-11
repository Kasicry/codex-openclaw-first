package com.kasicry.openclawnews.worker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CollectionController.class)
class CollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollectionService collectionService;

    @Test
    void collectionIsForbiddenByDefault() throws Exception {
        mockMvc.perform(post("/api/news/collect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sources\":[\"openai\"],\"keywords\":[\"AI\"]}"))
                .andExpect(status().isForbidden());
    }
}
