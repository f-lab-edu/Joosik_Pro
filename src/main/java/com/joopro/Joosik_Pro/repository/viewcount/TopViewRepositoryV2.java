package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;

import java.util.LinkedHashMap;

public interface TopViewRepositoryV2 {

    LinkedHashMap<Long, Post> getPopularPosts();

    Post returnPost(Long postId);

    void updateCacheWithDBAutomatically();

}
