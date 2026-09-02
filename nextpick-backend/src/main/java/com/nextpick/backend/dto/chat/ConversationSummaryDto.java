package com.nextpick.backend.dto.chat;

import com.nextpick.backend.entity.ChatConversation;

// DTO ligero para listar el historial en la sidebar (agrupado por fecha en el frontend)
public record ConversationSummaryDto(
        Long id,
        String title,
        String updatedAt
) {
    public static ConversationSummaryDto fromEntity(ChatConversation conversation) {
        return new ConversationSummaryDto(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getUpdatedAt().toString()
        );
    }
}
