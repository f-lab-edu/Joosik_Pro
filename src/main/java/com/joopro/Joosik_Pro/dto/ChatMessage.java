package com.joopro.Joosik_Pro.dto;

import lombok.Data;

@Data
public class ChatMessage {
    private String roomId;
    private String sender;
    private String content;
}
