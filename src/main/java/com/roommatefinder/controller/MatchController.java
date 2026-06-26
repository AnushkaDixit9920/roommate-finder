package com.roommatefinder.controller;

import com.roommatefinder.dto.MatchRequestDTO;
import com.roommatefinder.entity.User;
import com.roommatefinder.repository.UserRepository;
import com.roommatefinder.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final UserRepository userRepository;

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<Map<String, String>> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long receiverId) {
        Long senderId = getUserId(userDetails);
        String message = matchService.sendMatchRequest(senderId, receiverId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<Map<String, String>> acceptRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        Long userId = getUserId(userDetails);
        String message = matchService.respondToRequest(requestId, userId, true);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<Map<String, String>> rejectRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        Long userId = getUserId(userDetails);
        String message = matchService.respondToRequest(requestId, userId, false);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/sent/{requestId}")
    public ResponseEntity<Map<String, String>> withdrawRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        Long senderId = getUserId(userDetails);
        String message = matchService.withdrawMatchRequest(requestId, senderId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<MatchRequestDTO>> getSentRequests(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(matchService.getSentRequests(getUserId(userDetails)));
    }

    @GetMapping("/received")
    public ResponseEntity<List<MatchRequestDTO>> getReceivedRequests(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(matchService.getReceivedRequests(getUserId(userDetails)));
    }

    @GetMapping("/accepted")
    public ResponseEntity<List<MatchRequestDTO>> getAcceptedMatches(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(matchService.getAcceptedMatches(getUserId(userDetails)));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
