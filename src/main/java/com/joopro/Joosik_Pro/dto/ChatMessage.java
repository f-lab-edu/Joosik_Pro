package com.joopro.Joosik_Pro.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessage {
    private String roomId;
    private String sender;
    private String content;
}
