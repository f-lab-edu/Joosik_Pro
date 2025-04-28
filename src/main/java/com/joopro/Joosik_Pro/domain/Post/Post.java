package com.joopro.Joosik_Pro.domain.Post;

import com.joopro.Joosik_Pro.domain.BaseEntity;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Opinion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // 상속 전략 설정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Post extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "article_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Opinion> opinionList = new ArrayList<>();

    @Setter
    private String content;

    private Long viewCount;

    public Post(String content, Member member) {
        this.content = content;
        this.setMember(member);
        this.viewCount = 0L;
    }

    public void setMember(Member member) {
        this.member = member;
        member.addPosts(this);
    }

    public void addOpinion(Opinion opinion) {
        opinionList.add(opinion);
    }

    public void increaseViewCount(Long viewCount) {
        this.viewCount += viewCount;
    }

}