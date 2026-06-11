package com.kasicry.openclawnews.briefing;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TelegramClientTest {

    @Test
    void retriesTransientTelegramFailures() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        String url = "https://api.telegram.org/bottest-token/sendMessage";
        server.expect(requestTo(url)).andRespond(withServerError());
        server.expect(requestTo(url)).andRespond(withServerError());
        server.expect(requestTo(url)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        TelegramClient client = new TelegramClient(
                restTemplate,
                "test-token",
                "test-chat",
                3
        );

        client.send("Daily briefing");

        server.verify();
    }
}
