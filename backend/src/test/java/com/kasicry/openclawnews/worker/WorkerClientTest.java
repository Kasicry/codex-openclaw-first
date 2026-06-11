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
                        "{\"articles\":[{\"source\":\"openai\",\"title\":\"Codex\","
                                + "\"url\":\"https://example.com/codex\",\"published_at\":null,"
                                + "\"content\":\"details\",\"matched_keywords\":[\"Codex\"],"
                                + "\"related_sources\":[\"openai\",\"example\"]}],"
                                + "\"sources\":[{\"source\":\"openai\","
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
        assertThat(response.getArticles().get(0).getMatchedKeywords()).containsExactly("Codex");
        assertThat(response.getArticles().get(0).getRelatedSources())
                .containsExactly("openai", "example");
    }

    @Test
    void mapsPythonSummaryResponseToJavaDto() {
        server.expect(requestTo("http://worker.test/v1/summarize"))
                .andRespond(withSuccess(
                        "{\"title\":\"Codex\",\"core_content\":\"Summary\","
                                + "\"impact\":\"high\",\"developer_view\":\"Review it\"}",
                        MediaType.APPLICATION_JSON
                ));

        WorkerSummaryResponse response = workerClient.summarize(
                new WorkerSummaryRequest("Codex", "Details")
        );

        assertThat(response.getCoreContent()).isEqualTo("Summary");
        assertThat(response.getImpact()).isEqualTo("high");
        assertThat(response.getDeveloperView()).isEqualTo("Review it");
    }
}
