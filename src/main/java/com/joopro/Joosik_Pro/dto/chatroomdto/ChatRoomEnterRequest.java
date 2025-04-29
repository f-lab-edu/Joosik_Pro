package com.joopro.Joosik_Pro.dto.chatroomdto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChatRoomEnterRequest {
    private String roomId;
    private Long userId;
}
