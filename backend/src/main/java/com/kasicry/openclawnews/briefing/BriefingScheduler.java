package com.kasicry.openclawnews.briefing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BriefingScheduler {

    private final DailyBriefingService dailyBriefingService;
    private final boolean scheduleEnabled;
    private final boolean telegramSendEnabled;

    public BriefingScheduler(
            DailyBriefingService dailyBriefingService,
            @Value("${operations.schedule-enabled:false}") boolean scheduleEnabled,
            @Value("${operations.telegram-send-enabled:false}") boolean telegramSendEnabled
    ) {
        this.dailyBriefingService = dailyBriefingService;
        this.scheduleEnabled = scheduleEnabled;
        this.telegramSendEnabled = telegramSendEnabled;
    }

    @Scheduled(
            cron = "${operations.briefing-cron:0 0 8 * * *}",
            zone = "${operations.briefing-zone:Asia/Seoul}"
    )
    public void sendDailyBriefing() {
        if (!scheduleEnabled || !telegramSendEnabled) {
            return;
        }
        dailyBriefingService.sendToday();
    }
}
