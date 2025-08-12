package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.dto.PostDtoResponse;

import java.util.LinkedHashMap;

public interface TopViewRepositoryV2 {

    LinkedHashMap<Long, PostDtoResponse> getPopularPosts();

    PostDtoResponse returnPost(Long postId);

    void updateCacheWithDBAutomatically();

}
