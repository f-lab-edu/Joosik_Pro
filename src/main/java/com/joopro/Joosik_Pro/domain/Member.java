package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    private String password;

    private String email;

    private LocalDateTime date_created;

    @OneToMany(mappedBy = "member")
    private List<StockMembership> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Article> articles = new ArrayList<>();

    @OneToMany(mappedBy = "opinion")
    private List<Opinion> opinions = new ArrayList<>();

    // 연관관계 메서드
    public void addArticle(Article article){
        articles.add(article);
        article.setMember(this);
    }

    // 생성 메서드
    public static Member createMember(String name, String password, String email){
        Member member = new Member();
        member.setName(name);
        member.setPassword(password);
        member.setEmail(email);
        return member;
    }

}
