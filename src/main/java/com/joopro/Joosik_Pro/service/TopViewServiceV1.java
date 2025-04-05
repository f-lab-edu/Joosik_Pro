package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.repository.viewcount.TopViewRepositoryImplV1;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopViewServiceV1 {

    private final TopViewRepositoryImplV1 topViewRepositoryImplV1;

    // 맨 처음 Top10SingleStockPost 리스트에 값 넣기
    public void getDailyTop10Articles(){
        topViewRepositoryImplV1.getDailyTop10Article();
    }

    public void increaseViewCount(Long postId){
        topViewRepositoryImplV1.increaseViewCount(postId);
    }

    @Scheduled(fixedRate = 600000)
    public void refreshTop10(){
        topViewRepositoryImplV1.refreshTop10();
    }


}
