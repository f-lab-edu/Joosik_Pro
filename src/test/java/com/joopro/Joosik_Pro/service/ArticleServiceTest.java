package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.VsStockPost;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
class ArticleServiceTest {

    @Autowired
    SingleArticleService singleArticleService;
    @Autowired EntityManager em;

    @Test
    void saveSingleStockPost() {
        // given
        Article article = new Article();
        article.setContent("테슬라 지지자들에게 중요한 시험대(gut check moment)라고 표현하며, 현재의 하락이 오히려 좋은 매수 기회라고 주장하고 있다.");
        Stock stock = Stock.createStock("테슬라", "TSLA", "IT");
        SingleStockPost singleStockPost = SingleStockPost.createSingleStockPost(article, stock);

        // when
        singleArticleService.saveSingleStockPost(singleStockPost);
        em.persist(article);
        em.persist(stock);

        // then
        Assertions.assertThat(singleStockPost).isEqualTo(singleArticleService.findSingleStockPostByPostId(singleStockPost.getId()));

    }

    @Test
    void saveVsStockPost() {
        // given
        Article article = new Article();
        Stock stock = Stock.createStock("테슬라", "TSLA", "IT");
        article.setContent("테슬라 지지자들에게 중요한 시험대(gut check moment)라고 표현하며, 현재의 하락이 오히려 좋은 매수 기회라고 주장하고 있다. " +
                "\n 엔비디아는 지난 2년여간 인공지능(AI) 분야에서 독보적인 선두주자로 자리 잡으며 2023년 초 이후 주가가 600% 이상 상승했고, " +
                "시가총액은 약 3조 달러에 육박했다");

        Stock stock2 = Stock.createStock("엔비디아", "NVDA", "AI");
        VsStockPost vsStockPost = VsStockPost.createVsStockPost(stock, stock2, article);

        //when
        singleArticleService.saveVsStockPost(vsStockPost);
        em.persist(article);
        em.persist(stock);
        em.persist(stock2);

        //then
        Assertions.assertThat(vsStockPost).isEqualTo(singleArticleService.findVsStockPostByPostId(vsStockPost.getId()));

    }

    @Test
    void findSingleStockPostByContent() {
        //given
        Article article = new Article();
        article.setContent("테슬라 지지자들에게 중요한 시험대(gut check moment)라고 표현하며, 현재의 하락이 오히려 좋은 매수 기회라고 주장하고 있다.");
        Stock stock = Stock.createStock("테슬라", "TSLA", "IT");
        SingleStockPost singleStockPost1 = SingleStockPost.createSingleStockPost(article, stock);

        Article article2 = new Article();
        article2.setContent("엔비디아는 지난 2년여간 인공지능(AI) 분야에서 독보적인 선두주자로 자리 잡으며 2023년 초 이후 주가가 600% 이상 상승했고, " +
                "시가총액은 약 3조 달러에 육박했다");
        Stock stock2 = Stock.createStock("엔비디아", "NVDA", "AI");
        SingleStockPost singleStockPost2 = SingleStockPost.createSingleStockPost(article2, stock2);


        Article article3 = new Article();
        article3.setContent("엔비디아를 삽시다!");
        SingleStockPost singleStockPost3 = SingleStockPost.createSingleStockPost(article3, stock2);

        em.persist(article);
        em.persist(article2);
        em.persist(article3);
        em.persist(stock);
        em.persist(stock2);

        //when
        List<SingleStockPost> Nvdias = singleArticleService.findSingleStockPostByContent("엔비디아");

        //then
        Assertions.assertThat(Nvdias).contains(singleStockPost2, singleStockPost3);
        Assertions.assertThat(Nvdias.size()).isEqualTo(2);

    }

    // SingleStockPost는 CascadeType.All을 해놔서 stock을 persist하면 stock이 가진 singleStockPost도 자동으로 persist() 된다.
    @Test
    void findVsStockPostByContent() {
        //given
        Article article = new Article();
        article.setContent("테슬라 지지자들에게 중요한 시험대(gut check moment)라고 표현하며, 현재의 하락이 오히려 좋은 매수 기회라고 주장하고 있다.");
        Stock stock = Stock.createStock("테슬라", "TSLA", "IT");
        Stock stock2 = Stock.createStock("엔비디아", "NVDA", "AI");

        Article article2 = new Article();
        article2.setContent("테슬라를 삽시다");

        Article article3 = new Article();
        article3. setContent("엔비디아를 삽시다");

        Article article4 = new Article();
        article4. setContent("엔비디아가 테슬라보다 낫죠");

        VsStockPost vsStockPost = VsStockPost.createVsStockPost(stock, stock2, article);
        VsStockPost vsStockPost2 = VsStockPost.createVsStockPost(stock, stock2, article2);
        VsStockPost vsStockPost3 = VsStockPost.createVsStockPost(stock, stock2, article3);
        VsStockPost vsStockPost4 = VsStockPost.createVsStockPost(stock, stock2, article4);


        singleArticleService.saveVsStockPost(vsStockPost);
        singleArticleService.saveVsStockPost(vsStockPost2);
        singleArticleService.saveVsStockPost(vsStockPost3);
        singleArticleService.saveVsStockPost(vsStockPost4);
        em.persist(article);
        em.persist(article2);
        em.persist(article3);
        em.persist(article4);
        em.persist(stock);
        em.persist(stock2);

        //when
        List<VsStockPost> Nvdias = singleArticleService.findVsStockPostByContent("엔비디아");

        //then
        Assertions.assertThat(Nvdias).contains(vsStockPost3, vsStockPost4);
        Assertions.assertThat(Nvdias.size()).isEqualTo(2);

    }

    @Test
    void changeSingleStockPost() {
        //given
        Article article = new Article();
        article.setContent("테슬라 지지자들에게 중요한 시험대(gut check moment)라고 표현하며, 현재의 하락이 오히려 좋은 매수 기회라고 주장하고 있다.");
        Stock stock = Stock.createStock("테슬라", "TSLA", "IT");
        SingleStockPost singleStockPost1 = SingleStockPost.createSingleStockPost(article, stock);

        em.persist(article);
        em.persist(stock);
        article.setContent("엔비디아를 삽시다");

        //when
        singleArticleService.changeSingleStockPost(singleStockPost1.getId(), article, stock);

        //then
        Assertions.assertThat(singleStockPost1.getArticle().getContent()).isEqualTo("엔비디아를 삽시다");

    }

    @Test
    void changeVsStockPost() {
        //given
        Article article = new Article();
        article.setContent("테슬라 지지자들에게 중요한 시험대(gut check moment)라고 표현하며, 현재의 하락이 오히려 좋은 매수 기회라고 주장하고 있다.");
        Stock stock = Stock.createStock("테슬라", "TSLA", "IT");
        Stock stock2 = Stock.createStock("엔비디아", "NVDA", "AI");

        em.persist(article);
        em.persist(stock);
        em.persist(stock2);
        article.setContent("엔비디아를 삽시다");

        //when
        VsStockPost vsStockPost = VsStockPost.createVsStockPost(stock, stock2, article);
        singleArticleService.saveVsStockPost(vsStockPost);
        singleArticleService.changeVsStockPost(vsStockPost.getId(), article, stock, stock2);

        //then
        Assertions.assertThat(vsStockPost.getArticle().getContent()).isEqualTo("엔비디아를 삽시다");

    }

}