package com.nextpick.backend.service;

import com.nextpick.backend.dto.chat.ChatMessageDto;
import com.nextpick.backend.dto.chat.ConversationSummaryDto;
import com.nextpick.backend.entity.ChatConversation;
import com.nextpick.backend.entity.ChatMessage;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.repository.ChatConversationRepository;
import com.nextpick.backend.repository.ChatMessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    public List<ConversationSummaryDto> listConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(ConversationSummaryDto::fromEntity)
                .toList();
    }

    public List<ChatMessageDto> listMessages(Long conversationId, Long userId) {
        ChatConversation conversation = getOwnedConversation(conversationId, userId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()).stream()
                .map(ChatMessageDto::fromEntity)
                .toList();
    }

    public List<ChatAiService.ChatTurn> recentTurns(Long conversationId, Long userId, int limit) {
        ChatConversation conversation = getOwnedConversation(conversationId, userId);
        List<ChatMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        int from = Math.max(0, messages.size() - Math.max(1, limit));
        return messages.subList(from, messages.size()).stream()
                .map(message -> new ChatAiService.ChatTurn(
                        message.getSender() == ChatMessage.SenderType.USER ? "user" : "assistant",
                        message.getContent()))
                .toList();
    }

    /**
     * Obtiene la conversación indicada o crea una nueva si conversationId es null.
     * El título se autogenera a partir de los primeros caracteres del primer mensaje.
     */
    public ChatConversation getOrCreateConversation(Long conversationId, Long userId, User user, String firstMessage) {
        if (conversationId != null) {
            return getOwnedConversation(conversationId, userId);
        }

        String title = firstMessage.length() > 40 ? firstMessage.substring(0, 40) + "…" : firstMessage;

        ChatConversation conversation = ChatConversation.builder()
                .user(user)
                .title(title)
                .build();

        return conversationRepository.save(conversation);
    }

    public ChatMessage saveMessage(ChatConversation conversation, ChatMessage.SenderType sender, String content) {
        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .build();

        messageRepository.save(message);

        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return message;
    }

    private ChatConversation getOwnedConversation(Long conversationId, Long userId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversación no encontrada: " + conversationId));

        if (!conversation.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes acceso a esta conversación");
        }

        return conversation;
    }
}
