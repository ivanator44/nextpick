package com.nextpick.backend.controller;

import com.nextpick.backend.dto.chat.ChatMessageDto;
import com.nextpick.backend.dto.chat.ChatRequest;
import com.nextpick.backend.dto.chat.ConversationSummaryDto;
import com.nextpick.backend.entity.ChatConversation;
import com.nextpick.backend.entity.ChatMessage;
import com.nextpick.backend.entity.User;
import com.nextpick.backend.exception.AiNotConfiguredException;
import com.nextpick.backend.exception.UpstreamServiceException;
import com.nextpick.backend.repository.UserRepository;
import com.nextpick.backend.service.ChatAiService;
import com.nextpick.backend.service.ChatGroundingService;
import com.nextpick.backend.service.ChatService;
import com.nextpick.backend.service.AiErrorMessages;
import com.nextpick.backend.config.AiProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private static final long STREAM_TIMEOUT_GRACE_MS = 10_000;

    private final ChatService chatService;
    private final ChatAiService chatAiService;
    private final UserRepository userRepository;
    private final ChatGroundingService groundingService;
    private final AiProperties aiProperties;

    private final ExecutorService chatStreamingExecutor;

    @GetMapping("/conversations")
    public List<ConversationSummaryDto> getConversations(Authentication authentication) {
        User user = currentUser(authentication);
        return chatService.listConversations(user.getId());
    }

    @GetMapping("/conversations/{id}/messages")
    public List<ChatMessageDto> getMessages(@PathVariable Long id, Authentication authentication) {
        User user = currentUser(authentication);
        return chatService.listMessages(id, user.getId());
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        User user = currentUser(authentication);
        long streamTimeout = Math.max(70_000,
                aiProperties.requestTimeout().toMillis() + STREAM_TIMEOUT_GRACE_MS);
        SseEmitter emitter = new SseEmitter(streamTimeout);
        AtomicBoolean disconnected = new AtomicBoolean(false);
        emitter.onTimeout(() -> disconnected.set(true));
        emitter.onError(error -> disconnected.set(true));
        emitter.onCompletion(() -> disconnected.set(true));

        chatStreamingExecutor.execute(() -> {
            try {
                ChatConversation conversation = chatService.getOrCreateConversation(
                        request.conversationId(), user.getId(), user, request.content());

                emitter.send(SseEmitter.event().name("meta")
                        .data(Map.of("conversationId", conversation.getId())));

                chatService.saveMessage(conversation, ChatMessage.SenderType.USER, request.content());

                List<ChatAiService.ChatTurn> history = chatService.recentTurns(
                        conversation.getId(), user.getId(), aiProperties.maxHistoryMessages());
                List<ChatAiService.GroundedTitle> references = groundingService.ground(request.content());
                if (!references.isEmpty()) {
                    emitter.send(SseEmitter.event().name("references").data(references));
                }

                StringBuilder fullReply = new StringBuilder();

                chatAiService.streamResponse(new ChatAiService.AiChatRequest(history, references), token -> {
                    if (disconnected.get()) return;
                    try {
                        fullReply.append(token);
                        emitter.send(SseEmitter.event().name("delta").data(Map.of("text", token)));
                    } catch (Exception e) {
                        disconnected.set(true);
                    }
                }, disconnected::get);

                if (disconnected.get()) return;
                String completeReply = fullReply.toString().trim();
                if (!completeReply.isBlank()) {
                    chatService.saveMessage(conversation, ChatMessage.SenderType.ASSISTANT, completeReply);
                }

                emitter.send(SseEmitter.event().name("done")
                        .data(Map.of("conversationId", conversation.getId())));
                emitter.complete();
            } catch (Exception ex) {
                if (!disconnected.get()) {
                    try {
                        emitter.send(SseEmitter.event().name("error")
                                .data(Map.of("message", publicErrorMessage(ex))));
                        emitter.complete();
                    } catch (Exception sendFailure) {
                        emitter.completeWithError(ex);
                    }
                }
            }
        });

        return emitter;
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Usuario no encontrado"));
    }

    private String publicErrorMessage(Exception error) {
        if ((error instanceof UpstreamServiceException || error instanceof AiNotConfiguredException)
                && error.getMessage() != null && !error.getMessage().isBlank()) {
            return error.getMessage();
        }
        return AiErrorMessages.UNEXPECTED;
    }
}
