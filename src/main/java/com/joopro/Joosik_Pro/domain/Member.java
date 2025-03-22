package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자, 외부에서 사용 방지
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

    @OneToMany(mappedBy = "member")
    private List<Opinion> opinions = new ArrayList<>();

    @Builder
    public Member(String name, String password, String email, LocalDateTime date_created){
        this.name = name;
        this.password = password;
        this.email = email;
        this.date_created = (date_created != null) ? date_created : LocalDateTime.now();
    }

    // Article에서 사용할 메서드, articles 리스트에 추가
    public void addArticle(Article article){
        articles.add(article);
    }

    // Opinion에서 사용할 메서드, opinions 리스트에 추가
    public void addOpinion(Opinion opinion){
        opinions.add(opinion);
    }

    // StockMemberShip에서 사용할 메서드, memberships 리스트에 추가
    public void addStockMemberShip(StockMembership stockMembership){
        memberships.add(stockMembership);
    }

    // 변경 메서드 추가 (setter 대체)
    public void updateMember(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
    }

}
