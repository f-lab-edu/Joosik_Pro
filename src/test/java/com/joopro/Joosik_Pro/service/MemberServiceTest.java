package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Member;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired MemberService memberService;

    @Test
    void join() {
        //given
        Member member = Member.createMember("주식", "프로", "abc");

        //when
        Long saveId = memberService.join(member);

        //then
        Assertions.assertThat(member).isEqualTo(memberService.findOne(saveId));
    }

    @Test
    void update() {
        //given
        Member member = Member.createMember("주식", "프로", "abc");

        //when
        Long saveId = memberService.join(member);
        memberService.update(saveId, "주식2", "프로2", "abc2");

        //then
        Assertions.assertThat(member).isEqualTo(memberService.findOne(saveId));
        Assertions.assertThat(member.getName()).isEqualTo("주식2");
    }

    @Test
    void getMembers() {
        //given
        Member member1 = Member.createMember("주식", "프로", "abc");
        Member member2 = Member.createMember("주식2", "프로2", "abc2");

        //when
        Long saveId1 = memberService.join(member1);
        Long saveId2 = memberService.join(member2);
        List<Member> memberList = memberService.getMembers();

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
        Long saveId1 = memberService.join(member1);
        Long saveId2 = memberService.join(member2);
        Long saveId3 = memberService.join(member3);
        List<Member> memberList = memberService.findByName("주식");

        //then
        Assertions.assertThat(memberList.size()).isEqualTo(2);
        Assertions.assertThat(memberList).contains(member1, member2);


    }
}