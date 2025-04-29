package com.joopro.Joosik_Pro.repository;

import com.joopro.Joosik_Pro.domain.ChatRoom;
import com.joopro.Joosik_Pro.domain.ChatRoomUser;
import com.joopro.Joosik_Pro.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Repository
@RequiredArgsConstructor
public class ChatRoomRepository {

    private final EntityManager em;

    // ✅ 채팅방 생성
    @Transactional
    public ChatRoom save(ChatRoom chatRoom) {
        em.persist(chatRoom);
        return chatRoom;
    }

    // ✅ roomKey로 채팅방 찾기
    public Optional<ChatRoom> findByRoomID(String roomId) {
        ChatRoom result = em.createQuery(
                        "SELECT c FROM ChatRoom c WHERE c.roomId = :roomKey", ChatRoom.class)
                .setParameter("roomKey", roomId)
                .getSingleResult();
        return Optional.ofNullable(result);
    }

    // ✅ 유저가 참여 중인 채팅방 roomKey 목록
    public List<String> findRoomKeysByUser(Long userId) {
        return em.createQuery(
                        "SELECT cru.chatRoom.roomId FROM ChatRoomUser cru WHERE cru.member.id = :userId", String.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    // ✅ 채팅방에 유저 추가 (입장)
    @Transactional
    public void addUserToRoom(Member member, ChatRoom chatRoom) {
        ChatRoomUser chatRoomUser = ChatRoomUser.createChatRoom(member, chatRoom);
        em.persist(chatRoomUser);
    }

    // ✅ 채팅방에서 유저 제거 (퇴장)
    @Transactional
    public void removeUserFromRoom(Long userId, String roomId) {
        try {
            ChatRoomUser cru = em.createQuery(
                            "SELECT cru FROM ChatRoomUser cru WHERE cru.member.id = :userId AND cru.chatRoom.id = :roomId", ChatRoomUser.class)
                    .setParameter("userId", userId)
                    .setParameter("roomId", roomId)
                    .getSingleResult();

            em.remove(cru);
        } catch (NoResultException e) {
            // 이미 나간 경우 예외 무시 가능
        }
    }

    // ✅ 특정 roomId(Long) 기준으로 유저 존재 여부
    public boolean hasParticipants(Long roomId) {
        Long count = em.createQuery(
                        "SELECT COUNT(cru) FROM ChatRoomUser cru WHERE cru.chatRoom.id = :roomId", Long.class)
                .setParameter("roomId", roomId)
                .getSingleResult();
        return count > 0;
    }

    // ✅ roomKey 기준으로 유저 존재 여부
    public boolean hasParticipantsByRoomKey(String roomKey) {
        Long count = em.createQuery(
                        "SELECT COUNT(cru) FROM ChatRoomUser cru WHERE cru.chatRoom.roomKey = :roomKey", Long.class)
                .setParameter("roomKey", roomKey)
                .getSingleResult();
        return count > 0;
    }
}