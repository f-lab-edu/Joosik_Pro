package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자, 외부에서 출력 방지
public class Opinion {

    @Id @GeneratedValue
    @Column(name = "opinion_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    private String comment;

    private LocalDateTime date_created;

    private long like_sum;

    private long dislike_sum;

    @Builder
    public Opinion(String comment, LocalDateTime date_created){
        this.comment = comment;
        this.like_sum = 0L;
        this.dislike_sum = 0L;
        this.date_created = (date_created != null) ? date_created : LocalDateTime.now();
    }

    public static Opinion createOpinion(String comment, Article article, Member member){
        Opinion opinion = Opinion.builder()
                .comment(comment)
                .build();
        opinion.setArticle(article);
        opinion.setMember(member);
        return opinion;
    }


    // 연관관계 편의 메서드
    public void setArticle(Article article){
        this.article = article;
        article.addOpinion(this);
    }

    // 연관관계 편의 메서드
    public void setMember(Member member){
        this.member = member;
        member.addOpinion(this);
    }


    public void press_like(){
        like_sum++;
    }

    public void press_dislike(){
        dislike_sum++;
    }

}
