package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.web.ConditionalOnEnabledResourceChain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
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


}
