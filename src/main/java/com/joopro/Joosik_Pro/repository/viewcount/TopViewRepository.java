package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 자주 조회되는 게시글 캐시 기능
 * 자주 조회되는 게시글 조회수 Bulk 기능
 */
public interface TopViewRepository {

    // 게시물 조회수 증가
    void bulkUpdatePostViews(Long postId);

    LinkedHashMap<Long, Post> getPopularPosts();

}
