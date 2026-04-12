package spammy.eve.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import spammy.eve.domain.sde.SdeService;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class SdeScheduler {

    private final SdeService sdeUpdateService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("앱 시작 - SDE 버전 체크");
        sdeUpdateService.checkAndUpdate();
    }
}
