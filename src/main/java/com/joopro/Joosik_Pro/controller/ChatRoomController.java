package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.chatroomdto.ChatRoomCreateRequest;
import com.joopro.Joosik_Pro.dto.chatroomdto.ChatRoomEnterRequest;
import com.joopro.Joosik_Pro.dto.chatroomdto.ChatRoomLeaveRequest;
import com.joopro.Joosik_Pro.service.ChatService.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/create")
    public Result<String> createRoom(@RequestBody ChatRoomCreateRequest request) {
        String roomId = chatRoomService.createRoom(request.getName());
        return Result.ok(roomId); // 클라이언트는 이 roomId로 입장 요청
    }


    @PostMapping("/enter")
    public Result<String> enterRoom(@RequestBody ChatRoomEnterRequest request) {
        chatRoomService.enterRoom(request);
        return Result.ok("채팅방 입장 완료");
    }

    @PostMapping("/leave")
    public Result<String> leaveRoom(@RequestBody ChatRoomLeaveRequest request) {
        chatRoomService.leaveRoom(request);
        return Result.ok("채팅방 퇴장 완료");
    }

}