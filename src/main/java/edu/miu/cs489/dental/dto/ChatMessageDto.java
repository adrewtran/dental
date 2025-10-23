package edu.miu.cs489.dental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String message;
    private String sender; // "user" or "bot"
    private Long timestamp;
}

