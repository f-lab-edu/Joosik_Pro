package com.joopro.Joosik_Pro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Opinion;
import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.dto.opiniondto.CreateOpinionDto;
import com.joopro.Joosik_Pro.dto.opiniondto.OpinionDtoResponse;
import com.joopro.Joosik_Pro.service.OpinionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(locations = "classpath:application-test.properties")
@WebMvcTest(OpinionController.class)
class OpinionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpinionService opinionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("일반 댓글을 저장할 수 있다.")
    void saveOpinionNormalOpinion() throws Exception {
        // given
        CreateOpinionDto createOpinionDto = new CreateOpinionDto();
        createOpinionDto.setComment("이건 일반 댓글입니다.");

        OpinionDtoResponse response = OpinionDtoResponse.of(
                this.createDummyOpinion("이건 일반 댓글입니다.")
        );

        Mockito.when(opinionService.SaveOpinion(any(), anyLong(), anyLong()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/opinion/save")
                        .param("memberId", "1")
                        .param("postId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOpinionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.opinionContent").value("이건 일반 댓글입니다."));
    }

    @Test
    @DisplayName("대댓글을 저장할 수 있다.")
    void saveOpinionNestedOpinion() throws Exception {
        // given
        CreateOpinionDto createOpinionDto = new CreateOpinionDto();
        createOpinionDto.setComment("이건 대댓글입니다.");
        createOpinionDto.setParentOpinionId(100L); // 부모 댓글 ID

        OpinionDtoResponse response = OpinionDtoResponse.of(
                this.createDummyOpinion("이건 대댓글입니다.")
        );

        Mockito.when(opinionService.SaveOpinion(any(), anyLong(), anyLong()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/opinion/save")
                        .param("memberId", "1")
                        .param("postId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOpinionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.opinionContent").value("이건 대댓글입니다."));
    }

    public Opinion createDummyOpinion(String comment){
        Member member = Member.builder()
                .name("테스트유저")
                .password("aaa")
                .email("aaa")
                .build();

        Stock stock = Stock.builder()
                .companyName("Apple")
                .sector("Tech")
                .ticker("AAPL")
                .build();

        SingleStockPost singleStockPost = SingleStockPost.makeSingleStockPost("애플 분석", member, stock);

        return Opinion.createOpinion(comment, singleStockPost, member);
    }

}
