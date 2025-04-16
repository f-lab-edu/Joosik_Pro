package com.joopro.Joosik_Pro.service.TopViewService;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.viewcount.TopViewRepository;
import com.joopro.Joosik_Pro.repository.viewcount.TopViewRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class TopViewService {

    private final TopViewRepositoryV2 topViewRepository;

    // 조회수 Top100 Posts 가져오기 리스트에 값 넣기
    public List<Post> getPopularArticles(){
        LinkedHashMap<Long, Post> Top100Post =topViewRepository.getPopularPosts();
        return new ArrayList<>(Top100Post.values());
    }

    // cache에 Post 있다면 반환, 조회수도 한번에 올리기 위해서 내부적으로 처리
    public Post returnPost(Long postId){
        Post post = topViewRepository.returnPost(postId);
        return post;
    }


}
