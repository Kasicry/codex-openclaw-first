package com.kasicry.openclawnews.worker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WorkerClient {

    private final RestTemplate restTemplate;
    private final String workerBaseUrl;

    public WorkerClient(RestTemplate restTemplate, @Value("${worker.base-url}") String workerBaseUrl) {
        this.restTemplate = restTemplate;
        this.workerBaseUrl = workerBaseUrl;
    }

    public WorkerCollectResponse collect(WorkerCollectRequest request) {
        return restTemplate.postForObject(
                workerBaseUrl + "/v1/collect",
                request,
                WorkerCollectResponse.class
        );
    }

    public WorkerSummaryResponse summarize(WorkerSummaryRequest request) {
        return restTemplate.postForObject(
                workerBaseUrl + "/v1/summarize",
                request,
                WorkerSummaryResponse.class
        );
    }
}
