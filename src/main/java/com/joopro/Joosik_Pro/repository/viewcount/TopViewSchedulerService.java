package com.joopro.Joosik_Pro.repository.viewcount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopViewSchedulerService {

    private final TopViewRepositoryImplV3 topViewRepositoryImplV3;

    @Scheduled(cron = "${scheduler.topview.cron}")
//    @Transactional
    public void updateCacheWithDBAutomatically() {
        log.info("TopView 캐시 및 DB 업데이트 시작");
        topViewRepositoryImplV3.init();
    }

}
