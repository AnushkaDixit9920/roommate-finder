package com.roommatefinder.controller;

import com.roommatefinder.dto.ProfileDTO;
import com.roommatefinder.dto.ProfileRequest;
import com.roommatefinder.entity.User;
import com.roommatefinder.repository.UserRepository;
import com.roommatefinder.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ProfileDTO> createOrUpdateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileRequest request) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(profileService.createOrUpdateProfile(userId, request));
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @GetMapping("/matches")
    public ResponseEntity<List<ProfileDTO>> getMatches(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(profileService.getMatches(userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    // Upload profile picture — accepts multipart/form-data
    @PostMapping(value = "/me/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, String>> uploadProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        Long userId = getUserId(userDetails);
        String url = profileService.uploadProfilePicture(userId, file);
        return ResponseEntity.ok(java.util.Map.of("url", url));
    }

    // Search & Filter endpoint — all params optional
    @GetMapping("/search")
    public ResponseEntity<List<ProfileDTO>> searchProfiles(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer budgetMin,
            @RequestParam(required = false) Integer budgetMax,
            @RequestParam(required = false) String sleepSchedule) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(profileService.searchProfiles(userId, city, budgetMin, budgetMax, sleepSchedule));
    }

    // Paginated matches — page is 0-based, default page size 6
    @GetMapping("/matches/paged")
    public ResponseEntity<Page<ProfileDTO>> getMatchesPaged(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Long userId = getUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(profileService.getMatchesPaged(userId, pageable));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
