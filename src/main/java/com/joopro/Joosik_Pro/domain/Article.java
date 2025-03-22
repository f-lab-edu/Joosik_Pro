package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.web.ConditionalOnEnabledResourceChain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자, 외부에서 사용 방지
public class Article {

    @Id @GeneratedValue
    @Column(name = "article_id")
    private Long id;

    @OneToOne(mappedBy = "article", fetch = FetchType.LAZY)
    private VsStockPost vsStockPost;

    @OneToOne(mappedBy = "article", fetch = FetchType.LAZY)
    private SingleStockPost singleStockPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<Opinion> opinionList = new ArrayList<>();

    private String content;

    private Long viewCount;

    private LocalDateTime date_created;

    @Builder
    public Article(String content, LocalDateTime date_created){
        this.content = content;
        this.viewCount = 0L;
        this.date_created = (date_created != null) ? date_created : LocalDateTime.now();
    }

    public static Article createArticle(String content, Member member){
        Article article = Article.builder()
                .content(content)
                .build();
        article.setMember(member);
        return article;
    }

    // SingleStockPost 생성 메서드
    public static SingleStockPost createSingleStockPost(Article article, Stock stock){
        SingleStockPost singleStockPost = new SingleStockPost();
        singleStockPost.setArticle(article);
        singleStockPost.setStock(stock);
        return singleStockPost;
    }

    // VsStockPost 생성 메서드
    public static VsStockPost createVsStockPost(Article article, Stock stock1, Stock stock2){
        VsStockPost vsStockPost = new VsStockPost();
        vsStockPost.setArticle(article);
        vsStockPost.setStocks(stock1, stock2);
        return vsStockPost;
    }

    // 멤버 연관관계 편의 메서드
    public void setMember(Member member){
        this.member = member;
        member.addArticle(this);
    }

    // SingleStockPost에서 사용할 메서드, SingleStockPost 지정
    public void assignSingleStockPost(SingleStockPost singleStockPost){
        this.singleStockPost = singleStockPost;
    }

    // VsStockPost에서 사용할 메서드, VsStopckPost 지정
    public void assignVsStockPost(VsStockPost vsStockPost){
        this.vsStockPost = vsStockPost;
    }

    // Opinion에서 사용할 메서드, opinion 리스트에 추가
    public void addOpinion(Opinion opinion){
        opinionList.add(opinion);
    }


}
