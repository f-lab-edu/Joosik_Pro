package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Opinion;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.dto.opiniondto.CreateOpinionDto;
import com.joopro.Joosik_Pro.dto.opiniondto.OpinionDtoResponse;
import com.joopro.Joosik_Pro.repository.OpinionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Transactional(readOnly = true)
@RequiredArgsConstructor
public class OpinionService {

    private final OpinionRepository opinionRepository;
    private final MemberService memberService;
    private final PostService postService;

    // 댓글 저장
    @Transactional
    public OpinionDtoResponse SaveOpinion(CreateOpinionDto createOpinionDto, Long memberId, Long postId){
        Member member = memberService.findByMemberIdReturnEntity(memberId);
        Post post = postService.findPostByPostId(postId);
        Opinion opinion = Opinion.createOpinion(createOpinionDto.getComment(), post, member);
        opinionRepository.save(opinion);
        return OpinionDtoResponse.of(opinion);
    }

    // OpinionId로 Opinion 찾기
    public Opinion findByOpinionId(Long opinionId){
        return opinionRepository.findById(opinionId);
    }

    // memberId로 Opinion 찾기
    public List<OpinionDtoResponse> findOpinionByMemberId(Long memberId){
        List<Opinion> opinionList= opinionRepository.findOpinionByMemberId(memberId);
        List<OpinionDtoResponse> opinionDtoResponses = opinionList.stream()
                .map(o -> OpinionDtoResponse.of(o))
                .toList();
        return opinionDtoResponses;
    }

    @Transactional
    public void changeOpinion(Long id, String comment){
        Opinion opinion = opinionRepository.findById(id);
        opinion.setComment(comment);
    }

    // Opinion 좋아요 누르기
    @Transactional
    public void press_like(Long id){
        Opinion opinion = opinionRepository.findById(id);
        opinion.press_like();
    }


    // Opinion 싫어요 누르기
    @Transactional
    public void press_dislike(Long id){
        Opinion opinion = opinionRepository.findById(id);
        opinion.press_dislike();
    }

}
