package com.nextpick.backend.service;

import com.nextpick.backend.entity.ChatConversation;
import com.nextpick.backend.entity.ChatMessage;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.repository.ChatConversationRepository;
import com.nextpick.backend.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ChatServiceTest {
    @Test
    void rejectsReadingAnotherUsersConversation() {
        ChatConversationRepository conversations = mock(ChatConversationRepository.class);
        ChatMessageRepository messages = mock(ChatMessageRepository.class);
        ChatService service = new ChatService(conversations, messages);
        User owner = User.builder().id(7L).build();
        when(conversations.findById(11L)).thenReturn(Optional.of(ChatConversation.builder().id(11L).user(owner).build()));

        assertThatThrownBy(() -> service.listMessages(11L, 8L)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(messages);
    }

    @Test
    void limitsHistoryToMostRecentTurnsAfterOwnershipCheck() {
        ChatConversationRepository conversations = mock(ChatConversationRepository.class);
        ChatMessageRepository messages = mock(ChatMessageRepository.class);
        ChatService service = new ChatService(conversations, messages);
        User owner = User.builder().id(7L).build();
        ChatConversation conversation = ChatConversation.builder().id(11L).user(owner).build();
        when(conversations.findById(11L)).thenReturn(Optional.of(conversation));
        when(messages.findByConversationIdOrderByCreatedAtAsc(11L)).thenReturn(List.of(
                ChatMessage.builder().sender(ChatMessage.SenderType.USER).content("uno").build(),
                ChatMessage.builder().sender(ChatMessage.SenderType.ASSISTANT).content("dos").build(),
                ChatMessage.builder().sender(ChatMessage.SenderType.USER).content("tres").build()));

        assertThat(service.recentTurns(11L, 7L, 2))
                .extracting(ChatAiService.ChatTurn::content).containsExactly("dos", "tres");
    }
}
