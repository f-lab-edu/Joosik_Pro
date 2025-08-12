package com.joopro.Joosik_Pro.dto;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.dto.stockdto.StockDtoResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.text.AbstractDocument;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class PostDtoResponse {

    private String member;
    private String content;
    private Long viewCount;

    @Builder
    private PostDtoResponse(Member member, String content, Long viewCount){
        this.member = member.getName();
        this.content = content;
        this.viewCount = (viewCount != null ? viewCount : 0L);
    }

    public static PostDtoResponse of(Post post){
        return PostDtoResponse.builder()
                .member(post.getMember())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .build();
    }

}
