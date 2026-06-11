package com.kasicry.openclawnews.worker;

import com.kasicry.openclawnews.config.HttpClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(
        value = WorkerClient.class,
        properties = "worker.base-url=http://worker.test"
)
@Import(HttpClientConfig.class)
class WorkerClientTest {

    @Autowired
    private WorkerClient workerClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void mapsPythonSnakeCaseResponseToJavaDto() {
        server.expect(requestTo("http://worker.test/v1/collect"))
                .andRespond(withSuccess(
                        "{\"articles\":[],\"sources\":[{\"source\":\"openai\","
                                + "\"status\":\"completed\",\"article_count\":2,\"duration_ms\":12,"
                                + "\"error\":null}],\"duplicate_count\":1,\"duration_ms\":20}",
                        MediaType.APPLICATION_JSON
                ));

        WorkerCollectRequest request = new WorkerCollectRequest();
        request.setSources(Collections.singletonList("openai"));

        WorkerCollectResponse response = workerClient.collect(request);

        assertThat(response.getSources()).hasSize(1);
        assertThat(response.getSources().get(0).getArticleCount()).isEqualTo(2);
        assertThat(response.getSources().get(0).getDurationMs()).isEqualTo(12);
        assertThat(response.getDuplicateCount()).isEqualTo(1);
    }
}
