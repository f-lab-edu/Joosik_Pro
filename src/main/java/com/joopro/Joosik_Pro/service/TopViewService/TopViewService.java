package com.joopro.Joosik_Pro.service.TopViewService;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.viewcount.TopViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopViewService {

    private final TopViewRepository topViewRepository;

    // 조회수 Top100 Posts 가져오기 리스트에 값 넣기
    public List<Post> getPopularArticles(){
        LinkedHashMap<Long, Post> Top100Post =topViewRepository.getPopularPosts();
        return new ArrayList<>(Top100Post.values());
    }

    // viewCount increase 시키는 로직
    public void bulkUpdatePostViews(Long postId){
        topViewRepository.bulkUpdatePostViews(postId);
    }


}
