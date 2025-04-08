package com.joopro.Joosik_Pro.dto;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.dto.stockdto.StockDtoResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.swing.text.AbstractDocument;

@Data
@AllArgsConstructor
public class PostDtoResponse {

    private String member;
    private String content;

    @Builder
    private PostDtoResponse(Member member, String content){
        this.member = member.getName();
        this.content = content;
    }

    public static PostDtoResponse of(Post post){
        return PostDtoResponse.builder()
                .member(post.getMember())
                .content(post.getContent())
                .build();
    }

}
