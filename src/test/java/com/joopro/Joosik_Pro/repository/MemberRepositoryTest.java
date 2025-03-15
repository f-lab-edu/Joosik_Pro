package com.joopro.Joosik_Pro.repository;

import com.joopro.Joosik_Pro.domain.Member;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired EntityManager em;
    @Autowired MemberRepository memberRepository;

    @Test
    void save() {
        //given
        Member member = Member.createMember("주식", "123", "abc");

        //when
        Long saveId = memberRepository.save(member);

        //then
        Assertions.assertThat(member).isEqualTo(memberRepository.findOne(saveId));

    }

    @Test
    void findAll() {
        //given
        Member member1 = Member.createMember("주식", "프로", "abc");
        Member member2 = Member.createMember("주식2", "프로2", "abc2");

        //when
        Long saveId1 = memberRepository.save(member1);
        Long saveId2 = memberRepository.save(member2);
        List<Member> memberList = memberRepository.findAll();

        //then
        Assertions.assertThat(memberList.size()).isEqualTo(2);
        Assertions.assertThat(memberList).contains(member1, member2);

    }

    @Test
    void findByName() {
        //given
        Member member1 = Member.createMember("주식", "프로", "abc");
        Member member2 = Member.createMember("주식", "프로", "abc");
        Member member3 = Member.createMember("다른 주식", "프로", "abc");

        //when
        Long saveId1 = memberRepository.save(member1);
        Long saveId2 = memberRepository.save(member2);
        Long saveId3 = memberRepository.save(member3);
        List<Member> memberList = memberRepository.findByName("주식");

        //then
        Assertions.assertThat(memberList.size()).isEqualTo(2);
        Assertions.assertThat(memberList).contains(member1, member2);
    }


}