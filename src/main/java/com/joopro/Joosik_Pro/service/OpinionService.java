package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Opinion;
import com.joopro.Joosik_Pro.dto.opiniondto.CreateOpinionDto;
import com.joopro.Joosik_Pro.dto.opiniondto.ReturnOpinionDto;
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
    private final SingleArticleService singleArticleService;
    private final VsArticleService vsArticleService;

    @Transactional
    public ReturnOpinionDto saveSingleArticleOpinion(CreateOpinionDto createOpinionDto, Long memberId, Long articleId){
        Member member = memberService.findByMemberIdReturnEntity(memberId);
        Article article;
        article = singleArticleService.findSingleStockPostByPostIdReturnEntity(articleId).getArticle();
        Opinion opinion = Opinion.makeOpinion(createOpinionDto.getComment(), member, article);
        opinionRepository.save(opinion);
        ReturnOpinionDto returnOpinionDto = new ReturnOpinionDto(memberId, articleId, opinion.getId());
        return returnOpinionDto;
    }

    @Transactional
    public ReturnOpinionDto saveVsArticleOpinion(CreateOpinionDto createOpinionDto, Long memberId, Long articleId){
        Member member = memberService.findByMemberIdReturnEntity(memberId);
        Article article;
        article = vsArticleService.findVsStockPostByPostIdReturnEntity(articleId).getArticle();
        Opinion opinion = Opinion.makeOpinion(createOpinionDto.getComment(), member, article);
        opinionRepository.save(opinion);
        ReturnOpinionDto returnOpinionDto = new ReturnOpinionDto(memberId, articleId, opinion.getId());
        return returnOpinionDto;
    }

    public Opinion findByOpinionId(Long opinionId){
        return opinionRepository.findById(opinionId);
    }

    public List<ReturnOpinionDto> findOpinionByMemberId(Long memberId){
        List<Opinion> opinionList= opinionRepository.findOpinionByMemberId(memberId);
        List<ReturnOpinionDto> returnOpinionDtos = opinionList.stream()
                .map(o -> new ReturnOpinionDto(o.getMember().getId(), o.getArticle().getId(), o.getId()))
                .toList();
        return returnOpinionDtos;
    }

    @Transactional
    public void changeOpinion(Long id, String comment, Member member, Article article){
        Opinion opinion = opinionRepository.findById(id);
        opinion.setComment(comment);
        opinion.setMember(member);
        opinion.setArticle(article);
    }

    @Transactional
    public void press_like(Long id){
        Opinion opinion = opinionRepository.findById(id);
        opinion.press_like();
    }

    @Transactional
    public void press_dislike(Long id){
        Opinion opinion = opinionRepository.findById(id);
        opinion.press_dislike();
    }

}
