package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Opinion;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OpinionServiceTest {

    @Autowired OpinionService opinionService;
    @Autowired EntityManager em;

    // Article Persist 안하면 테스트 통과 안됨
    @Test
    void saveOpinion() {
        //given
        String comment = "굿";
        Member member = Member.createMember("주식", "프로", "abc");
        Article article = new Article();
        article.setContent("테슬라 지지자들에게 중요한 시험대(gut check moment)라고 표현하며, 현재의 하락이 오히려 좋은 매수 기회라고 주장하고 있다.");

        em.persist(member);
        em.persist(article);
        Opinion opinion = Opinion.makeOpinion(comment, member, article);

        //when
        opinionService.saveOpinion(opinion);

        //then
        Assertions.assertThat(opinion).isEqualTo(opinionService.findByMemberId(member.getId()).get(0));
    }

    @Test
    void findByMemberId() {
        //given
        String comment = "굿";
        String comment2 = "배드";
        Member member = Member.createMember("주식", "프로", "abc");
        Member member2 = Member.createMember("주식2", "프로2", "abc2");
        Article article = new Article();
        article.setContent("테슬라 지지자들에게 중요한 시험대(gut check moment)라고 표현하며, 현재의 하락이 오히려 좋은 매수 기회라고 주장하고 있다.");

        em.persist(member);
        em.persist(member2);
        em.persist(article);
        Opinion opinion = Opinion.makeOpinion(comment, member, article);
        Opinion opinion2 = Opinion.makeOpinion(comment2, member, article);
        Opinion opinion3 = Opinion.makeOpinion(comment2, member2, article);

        opinionService.saveOpinion(opinion);
        opinionService.saveOpinion(opinion2);
        opinionService.saveOpinion(opinion3);

        //when
        List<Opinion> opinionList = opinionService.findByMemberId(member.getId());

        //then
        Assertions.assertThat(opinionList).contains(opinion2, opinion2);
        Assertions.assertThat(opinionList.size()).isEqualTo(2);
    }

    @Test
    void changeOpinion() {
        //given
        String comment = "굿";
        Member member = Member.createMember("주식", "프로", "abc");
        Article article = new Article();
        article.setContent("테슬라 지지자들에게 중요한 시험대(gut check moment)라고 표현하며, 현재의 하락이 오히려 좋은 매수 기회라고 주장하고 있다.");

        em.persist(member);
        em.persist(article);
        Opinion opinion = Opinion.makeOpinion(comment, member, article);
        opinionService.saveOpinion(opinion);

        String comment2 = "bad";

        //when
        opinionService.changeOpinion(opinion.getId(), comment2, member, article);

        //then
        Assertions.assertThat(opinionService.findByOpinionId(opinion.getId()).getComment()).isEqualTo("bad");

    }

    @Test
    void press_like_dis_like() {
        //given
        String comment = "굿";
        Member member = Member.createMember("주식", "프로", "abc");
        Article article = new Article();
        article.setContent("테슬라 지지자들에게 중요한 시험대(gut check moment)라고 표현하며, 현재의 하락이 오히려 좋은 매수 기회라고 주장하고 있다.");

        em.persist(member);
        em.persist(article);
        Opinion opinion = Opinion.makeOpinion(comment, member, article);
        opinionService.saveOpinion(opinion);

        //when, then
        Assertions.assertThat(opinion.getLike_sum()).isEqualTo(0);
        opinionService.press_like(opinion.getId());
        Assertions.assertThat(opinion.getLike_sum()).isEqualTo(1);

        Assertions.assertThat(opinion.getDislike_sum()).isEqualTo(0);
        opinionService.press_dislike(opinion.getId());
        Assertions.assertThat(opinion.getDislike_sum()).isEqualTo(1);
    }

}