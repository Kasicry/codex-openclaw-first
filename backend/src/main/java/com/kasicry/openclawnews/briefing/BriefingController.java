package com.kasicry.openclawnews.briefing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/briefing")
public class BriefingController {

    private final TelegramSendService telegramSendService;
    private final DailyBriefingService dailyBriefingService;
    private final boolean sendEnabled;

    public BriefingController(
            TelegramSendService telegramSendService,
            DailyBriefingService dailyBriefingService,
            @Value("${operations.telegram-send-enabled:false}") boolean sendEnabled
    ) {
        this.telegramSendService = telegramSendService;
        this.dailyBriefingService = dailyBriefingService;
        this.sendEnabled = sendEnabled;
    }

    @GetMapping("/preview/today")
    public BriefingPreview previewToday() {
        return dailyBriefingService.previewToday();
    }

    @PostMapping("/send")
    public void send(@Valid @RequestBody BriefingSendRequest request) {
        requireSendEnabled();
        telegramSendService.send(request.getText());
    }

    @PostMapping("/send/today")
    public BriefingSendResult sendToday() {
        requireSendEnabled();
        return new BriefingSendResult(dailyBriefingService.sendToday());
    }

    private void requireSendEnabled() {
        if (!sendEnabled) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Telegram send requires explicit approval and configuration"
            );
        }
    }
}
