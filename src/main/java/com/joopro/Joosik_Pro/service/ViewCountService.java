package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Post.VsStockPost;
import com.joopro.Joosik_Pro.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViewCountService {

    private final PostRepository postRepository;

    @Transactional
    public void increaseViewCount(Long id){
        Post post = postRepository.findById(id);
        post.increaseViewCount(1L);
    }

    public List<Post> findPopularPost(){
        return postRepository.getPopularArticles();
    }


}
