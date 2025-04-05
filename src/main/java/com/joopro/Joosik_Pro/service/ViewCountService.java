package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.SingleStockPost;
import com.joopro.Joosik_Pro.domain.VsStockPost;
import com.joopro.Joosik_Pro.repository.SingleStockPostRepository;
import com.joopro.Joosik_Pro.repository.VsStockPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViewCountService {

    private final SingleStockPostRepository singleStockPostRepository;
    private final VsStockPostRepository vsStockPostRepository;

    @Transactional
    public void increaseSingleStockPostViewCount(Long id){
        SingleStockPost post = singleStockPostRepository.findById(id);
        Article article = post.getArticle();
        article.increaseViewCount(1L);
    }

    @Transactional
    public void increaseVsStockPostViewCount(Long id){
        VsStockPost post = vsStockPostRepository.findByVsStockPostId(id);
        Article article = post.getArticle();
        article.increaseViewCount(1L);
    }

    public List<SingleStockPost> findPopularSingleStockPostArticle(){
        return singleStockPostRepository.getPopularArticles();
    }

    public List<VsStockPost> findPopularVsStockPostArticle(){
        return vsStockPostRepository.getPopularArticles();
    }

}
