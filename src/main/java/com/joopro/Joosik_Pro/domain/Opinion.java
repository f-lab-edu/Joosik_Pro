package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
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

    public Opinion makeOpinion(String comment){
        Opinion opinion = new Opinion();
        opinion.setComment(comment);
        return opinion;
    }

    public void setArticle(Article article){
        setArticle(article);
        article.getOpinionList().add(this);
    }

    public void setMember(Member member){
        setMember(member);
        member.getOpinions().add(this);
    }


    public void press_like(){
        like_sum++;
    }

    public void press_dislike(){
        dislike_sum++;
    }

}
