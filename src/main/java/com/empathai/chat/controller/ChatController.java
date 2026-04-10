package com.empathai.chat.controller;

import com.empathai.chat.dto.*;
import com.empathai.chat.service.ChatService;
import com.empathai.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Send a message and get AI reply.
     * POST /api/chat/message
     */
    @PostMapping("/message")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal User currentUser,
            @RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = chatService.sendMessage(currentUser.getId(), request.getMessage());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all sessions for the current student (list, no messages).
     * GET /api/chat/sessions
     */
    @GetMapping("/sessions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ChatSessionResponse>> getSessions(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(chatService.getSessions(currentUser.getId()));
    }

    /**
     * Get full message history for a specific session.
     * GET /api/chat/session/{id}
     */
    @GetMapping("/session/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ChatSessionResponse> getSession(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(chatService.getSessionMessages(id, currentUser.getId()));
    }

    /**
     * Get today's usage stats.
     * GET /api/chat/usage
     */
    @GetMapping("/usage")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ChatUsageResponse> getUsage(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(chatService.getUsage(currentUser.getId()));
    }
}
