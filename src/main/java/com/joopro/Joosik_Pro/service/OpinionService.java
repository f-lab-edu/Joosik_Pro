package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Opinion;
import com.joopro.Joosik_Pro.repository.OpinionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Transactional(readOnly = true)
@RequiredArgsConstructor
public class OpinionService {

    private final OpinionRepository opinionRepository;

    @Transactional
    public void saveOpinion(Opinion opinion){
        opinionRepository.save(opinion);
    }

    public Opinion findByOpinionId(Long opinionId){
        return opinionRepository.findById(opinionId);
    }

    public List<Opinion> findByMemberId(Long memberId){
        return opinionRepository.findByMemberId(memberId);
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
