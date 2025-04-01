package com.joopro.Joosik_Pro.repository;

import com.joopro.Joosik_Pro.domain.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    // 회원 저장
    public Long save(Member member){
        em.persist(member);
        return member.getId();
    }

    // 회원 ID로 찾기
    public Member findOne(Long id){
        return em.find(Member.class, id);
    }

    // 회원 모두 찾기
    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    // 회원 이름으로 찾기
    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }

}
