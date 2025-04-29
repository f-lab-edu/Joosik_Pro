package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue
    @Column(name = "chat_room_id")
    private Long id;

    private String name; // 방 이름

    private String roomId;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomUser> participants = new ArrayList<>();

    @Builder
    public ChatRoom(String name, String roomId) {
        this.name = name;
        this.roomId = roomId;
    }

    public void addParticipant(ChatRoomUser chatRoomUser) {
        participants.add(chatRoomUser);
    }

}
