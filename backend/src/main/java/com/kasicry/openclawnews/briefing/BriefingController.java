package com.kasicry.openclawnews.briefing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/briefing")
public class BriefingController {

    private final TelegramClient telegramClient;
    private final boolean sendEnabled;

    public BriefingController(
            TelegramClient telegramClient,
            @Value("${operations.telegram-send-enabled:false}") boolean sendEnabled
    ) {
        this.telegramClient = telegramClient;
        this.sendEnabled = sendEnabled;
    }

    @PostMapping("/send")
    public void send(@Valid @RequestBody BriefingSendRequest request) {
        if (!sendEnabled) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Telegram send requires explicit approval and configuration"
            );
        }
        telegramClient.send(request.getText());
    }
}
