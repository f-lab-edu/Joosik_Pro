package com.joopro.Joosik_Pro.domain;

import com.joopro.Joosik_Pro.domain.Post.Post;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자, 외부에서 사용 방지
@Table(name = "member", indexes = { // @Table 어노테이션 추가
        @Index(name = "idx_member_name", columnList = "name") // name 컬럼에 인덱스 생성
})
// 인덱스 없을 때 type : All 로 테이블 풀 스캔 발생 -> 인덱스 추가
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    private String password;

    private String email;

    @OneToMany(mappedBy = "member")
    private List<StockMembership> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Opinion> opinions = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<ChatRoomUser> chatRoomUsers = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FirstComeEventParticipation> Eventparticipations = new ArrayList<>();

    @Builder
    public Member(String name, String password, String email){
        this.name = name;
        this.password = password;
        this.email = email;
    }

    // Post에서 사용할 메서드, Posts 리스트에 추가
    public void addPosts(Post post){
        posts.add(post);
    }

    // Opinion에서 사용할 메서드, opinions 리스트에 추가
    public void addOpinion(Opinion opinion){
        opinions.add(opinion);
    }

    // StockMemberShip에서 사용할 메서드, memberships 리스트에 추가
    public void addStockMemberShip(StockMembership stockMembership){
        memberships.add(stockMembership);
    }

    public void addChatRoomUsers(ChatRoomUser chatRoomUser){
        chatRoomUsers.add(chatRoomUser);
    }

    public void addEventParticipation(FirstComeEventParticipation participation) {
        Eventparticipations.add(participation);
    }


    // 변경 메서드 추가 (setter 대체)
    public void updateMember(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
    }

}
