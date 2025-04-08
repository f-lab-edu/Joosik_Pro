package com.joopro.Joosik_Pro.service.TopViewService;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.viewcount.TopViewRepositoryImplV1;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopViewServiceV1 {

    private final TopViewRepositoryImplV1 topViewRepositoryImplV1;

    // 조회수 Top100 Posts 가져오기 리스트에 값 넣기
    public List<Post> getPopularArticles(){
        LinkedHashMap<Long, Post> Top100Post =topViewRepositoryImplV1.getPopularPosts();
        return new ArrayList<>(Top100Post.values());

    }


    public void bulkUpdatePostViews(Long postId){
        topViewRepositoryImplV1.bulkUpdatePostViews(postId);

    }

    @Scheduled(fixedRate = 600000)
    public void refreshTop100(){
        topViewRepositoryImplV1.updateCache();
    }


}
