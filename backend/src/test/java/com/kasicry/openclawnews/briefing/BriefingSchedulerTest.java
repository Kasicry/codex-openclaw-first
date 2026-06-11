package com.kasicry.openclawnews.briefing;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class BriefingSchedulerTest {

    @Test
    void doesNotRunUnlessScheduleAndTelegramAreBothEnabled() {
        DailyBriefingService service = mock(DailyBriefingService.class);

        new BriefingScheduler(service, false, true).sendDailyBriefing();
        new BriefingScheduler(service, true, false).sendDailyBriefing();

        verify(service, never()).sendToday();
    }

    @Test
    void runsWhenScheduleAndTelegramAreEnabled() {
        DailyBriefingService service = mock(DailyBriefingService.class);

        new BriefingScheduler(service, true, true).sendDailyBriefing();

        verify(service).sendToday();
    }
}
