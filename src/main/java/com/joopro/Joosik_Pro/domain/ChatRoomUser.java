package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomUser {

    @Id
    @GeneratedValue
    @Column(name = "chat_room_user_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;


    public static ChatRoomUser createChatRoom(Member member,  ChatRoom chatRoom){
        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setMember(member);
        chatRoomUser.setChatRoom(chatRoom);
        return chatRoomUser;
    }


    public void setMember(Member member){
        this.member = member;
        member.addChatRoomUsers(this);
    }

    public void setChatRoom(ChatRoom chatRoom){
        this.chatRoom = chatRoom;
        chatRoom.addParticipant(this);
    }

}
