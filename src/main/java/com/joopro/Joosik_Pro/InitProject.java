package com.joopro.Joosik_Pro;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.VsStockPost;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InitProject {

    private final InitService initService;

    @PostConstruct
    public void init(){
        initService.initDB();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService{
        private final EntityManager em;

        public void initDB(){
            Stock stock1 = makeStock("Apple Inc.", "AAPL", "Technology");
            Stock stock2 = makeStock("Microsoft Corp.", "MSFT", "Technology");
            Stock stock3 = makeStock("Amazon.com Inc.", "AMZN", "Consumer Discretionary");
            Stock stock4 = makeStock("Alphabet Inc.", "GOOGL", "Communication Services");
            Stock stock5 = makeStock("Tesla Inc.", "TSLA", "Automotive");
            Stock stock6 = makeStock("Meta Platforms Inc.", "META", "Communication Services");
            Stock stock7 = makeStock("Netflix Inc.", "NFLX", "Communication Services");
            Stock stock8 = makeStock("NVIDIA Corp.", "NVDA", "Technology");
            Stock stock9 = makeStock("JPMorgan Chase & Co.", "JPM", "Financials");
            Stock stock10 = makeStock("Johnson & Johnson", "JNJ", "Healthcare");
            Stock stock11 = makeStock("Visa Inc.", "V", "Financials");
            Stock stock12 = makeStock("Procter & Gamble Co.", "PG", "Consumer Staples");
            Stock stock13 = makeStock("Walmart Inc.", "WMT", "Consumer Staples");
            Stock stock14 = makeStock("Coca-Cola Co.", "KO", "Consumer Staples");
            Stock stock15 = makeStock("Pfizer Inc.", "PFE", "Healthcare");

            em.persist(stock1);
            em.persist(stock2);
            em.persist(stock3);
            em.persist(stock4);
            em.persist(stock5);
            em.persist(stock6);
            em.persist(stock7);
            em.persist(stock8);
            em.persist(stock9);
            em.persist(stock10);
            em.persist(stock11);
            em.persist(stock12);
            em.persist(stock13);
            em.persist(stock14);
            em.persist(stock15);

            for (int i = 1; i <= 15; i++) {
                Article article = makeArticle("Single Stock Article Content #" + i, (long)(i * 100));
                SingleStockPost singlePost = makeSingleStockPost(article, stock1);
                em.persist(article);
                em.persist(singlePost);
            }

            for (int i = 1; i <= 15; i++) {
                Article vsArticle = makeArticle("Vs Stock Article Content #" + i, (long)(i * 150));
                VsStockPost vsPost = makevsStockPost(stock2, stock3, vsArticle);
                em.persist(vsArticle);
                em.persist(vsPost);
            }
        }

        private Stock makeStock(String companyName, String ticker, String sector){
            return Stock.createStock(companyName, ticker, sector);
        }

        private Article makeArticle(String content, Long viewCount){
            Article article = new Article();
            article.setContent(content);
            article.setViewCount(viewCount);
            return article;
        }

        private SingleStockPost makeSingleStockPost(Article article, Stock stock){
            return SingleStockPost.createSingleStockPost(article, stock);
        }

        private VsStockPost makevsStockPost(Stock stock1, Stock stock2, Article article){
            return VsStockPost.createVsStockPost(stock1, stock2, article);
        }
    }

}
