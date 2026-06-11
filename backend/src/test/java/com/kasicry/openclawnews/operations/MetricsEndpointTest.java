package com.kasicry.openclawnews.operations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class MetricsEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OperationalMetrics metrics;

    @Test
    void exposesApplicationMetricsWithoutSensitiveTags() throws Exception {
        metrics.recordSummary("success", 1_000_000);

        mockMvc.perform(get("/actuator/metrics/openclaw.summary.runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("openclaw.summary.runs"))
                .andExpect(jsonPath("$.available_tags[0].tag").value("result"));
    }
}
