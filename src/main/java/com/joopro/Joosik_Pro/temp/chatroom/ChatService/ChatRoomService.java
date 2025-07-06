package com.joopro.Joosik_Pro.temp.chatroom.ChatService;

import com.joopro.Joosik_Pro.temp.chatroom.ChatRoom;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.temp.chatroom.chatroomdto.ChatRoomEnterRequest;
import com.joopro.Joosik_Pro.temp.chatroom.chatroomdto.ChatRoomLeaveRequest;
import com.joopro.Joosik_Pro.temp.chatroom.ChatRoomRepository;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final RedisMessageListenerContainer redisContainer;
    private final MessageListenerAdapter messageListenerAdapter;
    private final ChatRoomRepository chatRoomRepository;

    private final MemberRepository memberRepository;

    private final Map<String, ChannelTopic> topics = new ConcurrentHashMap<>();

    public String createRoom(String name) {
        // 고유 roomId 생성 (UUID 또는 커스텀 생성 가능)
        String roomId = UUID.randomUUID().toString();

        ChatRoom room = ChatRoom.builder()
                .name(name)
                .roomId(roomId)
                .build();

        chatRoomRepository.save(room);
        subscribe(roomId);
        return roomId;
    }



    public void enterRoom(ChatRoomEnterRequest request) {
        Member member = memberRepository.findOne(request.getUserId());
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByRoomID(request.getRoomId());
        // 유저 DB에 입장 정보 저장
        // 예: chatUserRepository.save(new ChatUser(userId, roomId));
        chatRoomRepository.addUserToRoom(member, chatRoom.get());
        // Redis 채널 구독
        subscribe(request.getRoomId());
    }

    public void leaveRoom(ChatRoomLeaveRequest chatRoomLeaveRequest) {
        // DB에서 참여 정보 삭제
        chatRoomRepository.removeUserFromRoom(chatRoomLeaveRequest.getUserId(), chatRoomLeaveRequest.getRoomId());

        // 해당 방이 비었는지 확인 후 구독 제거
        if (isRoomEmpty(chatRoomLeaveRequest.getRoomId())) {
            unsubscribe(chatRoomLeaveRequest.getRoomId());
        }
    }

    public void subscribe(String roomId) {
        topics.computeIfAbsent(roomId, id -> {
            ChannelTopic topic = new ChannelTopic(id);
            redisContainer.addMessageListener(messageListenerAdapter, topic);
            return topic;
        });
    }

    public void unsubscribe(String roomId) {
        ChannelTopic topic = topics.remove(roomId);
        if (topic != null) {
            redisContainer.removeMessageListener(messageListenerAdapter, topic);
        }
    }

    public boolean isRoomEmpty(String roomId) {
        // 예: 참여 유저가 없으면 true
        // return !chatUserRepository.existsByRoomId(roomId);
        return false;
    }

}