package com.joopro.Joosik_Pro.domain;

import com.joopro.Joosik_Pro.domain.Post.Post;
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
    @JoinColumn(name = "post_id")
    private Post post;

    @Setter
    private String comment;

    private long like_sum;

    private long dislike_sum;

    @Builder
    public Opinion(String comment){
        this.comment = comment;
        this.like_sum = 0L;
        this.dislike_sum = 0L;
    }

    public static Opinion createOpinion(String comment, Post post, Member member){
        Opinion opinion = Opinion.builder()
                .comment(comment)
                .build();
        opinion.setPost(post);
        opinion.setMember(member);
        return opinion;
    }


    // 연관관계 편의 메서드
    public void setPost(Post post){
        this.post = post;
        post.addOpinion(this);
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
