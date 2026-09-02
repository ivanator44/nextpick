package com.nextpick.backend.dto.chat;

import com.nextpick.backend.entity.ChatMessage;

public record ChatMessageDto(
        Long id,
        String sender,
        String content,
        String createdAt
) {
    public static ChatMessageDto fromEntity(ChatMessage message) {
        return new ChatMessageDto(
                message.getId(),
                message.getSender().name(),
                message.getContent(),
                message.getCreatedAt().toString()
        );
    }
}
