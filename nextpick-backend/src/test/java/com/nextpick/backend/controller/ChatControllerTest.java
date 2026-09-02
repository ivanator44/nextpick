package com.nextpick.backend.controller;

import com.nextpick.backend.config.AiProperties;
import com.nextpick.backend.entity.ChatConversation;
import com.nextpick.backend.entity.ChatMessage;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.repository.UserRepository;
import com.nextpick.backend.service.ChatAiService;
import com.nextpick.backend.service.ChatGroundingService;
import com.nextpick.backend.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatControllerTest {
    private final ChatService chatService = mock(ChatService.class);
    private final ChatAiService ai = mock(ChatAiService.class);
    private final UserRepository users = mock(UserRepository.class);
    private final ChatGroundingService grounding = mock(ChatGroundingService.class);
    private final ExecutorService executor = synchronousExecutor();
    private final User user = User.builder().id(4L).email("ana@example.test").build();
    private final ChatConversation conversation = ChatConversation.builder().id(9L).user(user).build();
    private final AiProperties properties = new AiProperties("openai", "key", "", "http://localhost", "model",
            Duration.ofSeconds(1), Duration.ofSeconds(5), 12);

    @Test
    void persistsOnlyACompleteNonEmptyAssistantReply() {
        arrangeConversation();
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String> consumer = invocation.getArgument(1);
            consumer.accept("Hola ");
            consumer.accept("mundo");
            return null;
        }).when(ai).streamResponse(any(), any(), any());

        controller().streamChat(new com.nextpick.backend.dto.chat.ChatRequest(null, "Recomiéndame algo"), auth());

        verify(chatService).saveMessage(conversation, ChatMessage.SenderType.USER, "Recomiéndame algo");
        verify(chatService).saveMessage(conversation, ChatMessage.SenderType.ASSISTANT, "Hola mundo");
    }

    @Test
    void doesNotPersistPartialAssistantTextAfterProviderFailure() {
        arrangeConversation();
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String> consumer = invocation.getArgument(1);
            consumer.accept("parcial");
            throw new IllegalStateException("provider failed");
        }).when(ai).streamResponse(any(), any(), any());

        controller().streamChat(new com.nextpick.backend.dto.chat.ChatRequest(null, "Recomiéndame algo"), auth());

        verify(chatService, never()).saveMessage(conversation, ChatMessage.SenderType.ASSISTANT, "parcial");
    }

    private void arrangeConversation() {
        when(users.findByEmail("ana@example.test")).thenReturn(Optional.of(user));
        when(chatService.getOrCreateConversation(null, 4L, user, "Recomiéndame algo")).thenReturn(conversation);
        when(chatService.recentTurns(9L, 4L, 12)).thenReturn(List.of());
        when(grounding.ground("Recomiéndame algo")).thenReturn(List.of());
    }

    private ChatController controller() {
        return new ChatController(chatService, ai, users, grounding, properties, executor);
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken("ana@example.test", null, List.of());
    }

    private ExecutorService synchronousExecutor() {
        ExecutorService service = mock(ExecutorService.class);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        }).when(service).execute(any(Runnable.class));
        return service;
    }
}
