package com.roommatefinder.controller;

import com.roommatefinder.entity.User;
import com.roommatefinder.repository.MatchRequestRepository;
import com.roommatefinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin-only endpoints for user management and statistics.
 * Secured with @PreAuthorize — only users with ROLE_ADMIN can access.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final MatchRequestRepository matchRequestRepository;

    /** Dashboard stats: total users, total matches, pending requests */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        long totalUsers = userRepository.count();
        long totalMatches = matchRequestRepository.findAll().stream()
                .filter(m -> m.getStatus() == com.roommatefinder.entity.MatchRequest.Status.ACCEPTED)
                .count();
        long pendingRequests = matchRequestRepository.findAll().stream()
                .filter(m -> m.getStatus() == com.roommatefinder.entity.MatchRequest.Status.PENDING)
                .count();
        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalMatches", totalMatches,
                "pendingRequests", pendingRequests
        ));
    }

    /** List all users with basic info */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(u -> Map.of(
                        "id", (Object) u.getId(),
                        "name", u.getName(),
                        "email", u.getEmail(),
                        "enabled", u.isEnabled(),
                        "role", u.getRole() != null ? u.getRole() : "USER",
                        "createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /** Delete a user account (and cascade their data via JPA) */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        if (admin.getId().equals(userId)) {
            throw new RuntimeException("Cannot delete your own admin account");
        }
        userRepository.deleteById(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    /** Promote a user to ADMIN role */
    @PostMapping("/users/{userId}/promote")
    public ResponseEntity<Map<String, String>> promoteUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole("ADMIN");
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", user.getName() + " promoted to ADMIN"));
    }
}
