package com.joopro.Joosik_Pro.service.TopViewService;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.viewcount.TopViewRepositoryImplV2;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopViewServiceV2 {

    private final TopViewRepositoryImplV2 topViewRepositoryImplV2;

    // 조회수 Top100 Posts 가져오기 리스트에 값 넣기
    public List<Post> getPopularArticles(){
        LinkedHashMap<Long, Post> Top100Post =topViewRepositoryImplV2.getPopularPosts();
        return new ArrayList<>(Top100Post.values());

    }

    // viewCount increase 시키는 로직
    public void bulkUpdatePostViews(Long postId){
        topViewRepositoryImplV2.bulkUpdatePostViews(postId);
    }

    // Cache 값 데이터 데이터베이스에 투입 하고 새로운 TOP100 불러오기
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
