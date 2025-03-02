package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.VsStockPost;
import com.joopro.Joosik_Pro.repository.SingleStockPostRepository;
import com.joopro.Joosik_Pro.repository.VsStockPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final SingleStockPostRepository singleStockPostRepository;
    private final VsStockPostRepository vsStockPostRepository;

    @Transactional
    public void saveSingleStockPost(SingleStockPost post){
        singleStockPostRepository.save(post);
    }

    public SingleStockPost findSingleStockPost(Long id){
        return singleStockPostRepository.findById(id);
    }

    @Transactional
    public void saveVsStockPost(VsStockPost vsStockPost){
        vsStockPostRepository.save(vsStockPost);
    }

    public VsStockPost findVsStockPost(Long id){
        return vsStockPostRepository.findById(id);
    }

    public List<SingleStockPost> findSingleStockPostById(Long stockId){
        return singleStockPostRepository.findByStockId(stockId);
    }

    public List<VsStockPost> findVsStockPostById(Long stockId){
        return vsStockPostRepository.findByStockId(stockId);
    }

    public List<SingleStockPost> findAllSingleStockPost(){
        return singleStockPostRepository.findAll();
    }

    public List<VsStockPost> findAllVsStockPost(){
        return vsStockPostRepository.findAll();
    }

    @Transactional
    public void changeSingleStockPost(Long id, Article article, Stock stock){
        SingleStockPost singleStockPost = singleStockPostRepository.findById(id);
        singleStockPost.setStock(stock);
        singleStockPost.setArticle(article);
    }

    @Transactional
    public void changeVsStockPost(Long id, Article article, Stock stock1, Stock stock2){
        VsStockPost vsStockPost = vsStockPostRepository.findById(id);
        vsStockPost.setArticle(article);
        vsStockPost.setStock1(stock1);
        vsStockPost.setStock2(stock2);
    }




}
