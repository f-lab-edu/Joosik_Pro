package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.repository.viewcount.TopViewRepositoryImplV2;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopViewServiceV2 {

    private final TopViewRepositoryImplV2 topViewRepositoryImplV2;

    //맨 처음 cache에 값 넣기
    public void firstInitializeCache(){
        topViewRepositoryImplV2.initializeCache();
    }

    // viewCount increase 시키는 로직
    public void increaseViewCount(Long postId){
        topViewRepositoryImplV2.increaseViewCount(postId);
    }

    // Cache 값 데이터 데이터베이스에 투입 하고 새로운 TOP10 불러오기
    @Scheduled(fixedRate = 600000)
    public void refreshCache(){
        topViewRepositoryImplV2.updateViewCountsToDB();
        topViewRepositoryImplV2.initializeCache();
    }

    // cache 내 정렬
    @Scheduled(fixedRate = 30000)
    public void sortCacheData(){
        topViewRepositoryImplV2.sortCacheByViewCount();
    }

}
