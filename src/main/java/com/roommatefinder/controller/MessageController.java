package com.roommatefinder.controller;

import com.roommatefinder.dto.ConversationDTO;
import com.roommatefinder.dto.MessageDTO;
import com.roommatefinder.dto.SendMessageRequest;
import com.roommatefinder.entity.User;
import com.roommatefinder.repository.UserRepository;
import com.roommatefinder.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    /** Inbox — all accepted conversations with last-message preview */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.getConversations(getUserId(userDetails)));
    }

    /** Total unread message count (for nav badge) */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        int count = messageService.getTotalUnread(getUserId(userDetails));
        return ResponseEntity.ok(Map.of("count", count));
    }

    /** All messages in one conversation, marks them as read for the caller */
    @GetMapping("/conversation/{matchRequestId}")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long matchRequestId) {
        return ResponseEntity.ok(messageService.getMessages(matchRequestId, getUserId(userDetails)));
    }

    /** Send a message in a conversation */
    @PostMapping("/conversation/{matchRequestId}")
    public ResponseEntity<MessageDTO> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long matchRequestId,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(
                messageService.sendMessage(matchRequestId, getUserId(userDetails), request.getContent()));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
