package com.roommatefinder.service;

import com.roommatefinder.dto.CompatibilityBreakdown;
import com.roommatefinder.service.MatchSummaryService;
import com.roommatefinder.dto.ProfileDTO;
import com.roommatefinder.dto.ProfileRequest;
import com.roommatefinder.entity.Profile;
import com.roommatefinder.entity.User;
import com.roommatefinder.repository.ProfileRepository;
import com.roommatefinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CompatibilityService compatibilityService;
    private final MatchSummaryService matchSummaryService;

    private static final String UPLOAD_DIR = "uploads/profile-pictures/";

    // ── Rank → weight lookup (rank 1 = most important) ─────────────────────
    private static final int[] RANK_WEIGHTS = { 35, 25, 18, 12, 7, 3 };

    /**
     * Convert a ranked factor list (e.g. ["budget","sleep","cleanliness","noise","pets","smoking"])
     * into the 6 weight fields and apply to the profile.
     * If weights are supplied directly (via ProfileRequest), use those instead.
     */
    private void applyWeights(Profile profile, ProfileRequest request) {
        // Direct weights supplied — validate and apply
        if (request.getBudgetWeight() != null) {
            int total = nullToZero(request.getBudgetWeight())
                      + nullToZero(request.getSleepWeight())
                      + nullToZero(request.getCleanlinessWeight())
                      + nullToZero(request.getNoiseWeight())
                      + nullToZero(request.getPetsWeight())
                      + nullToZero(request.getSmokingWeight());
            if (total != 100) throw new RuntimeException("Weights must sum to 100 (got " + total + ")");

            profile.setBudgetWeight(request.getBudgetWeight());
            profile.setSleepWeight(request.getSleepWeight());
            profile.setCleanlinessWeight(request.getCleanlinessWeight());
            profile.setNoiseWeight(request.getNoiseWeight());
            profile.setPetsWeight(request.getPetsWeight());
            profile.setSmokingWeight(request.getSmokingWeight());
        }
        // Otherwise preserve existing weights (entity defaults handle first-time creation)
    }

    private int nullToZero(Integer v) { return v == null ? 0 : v; }

    // ── Create / Update ─────────────────────────────────────────────────────

    @Transactional
    public ProfileDTO createOrUpdateProfile(Long userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(userId).orElse(new Profile());
        profile.setUser(user);
        profile.setCity(request.getCity());
        profile.setPreferredArea(request.getPreferredArea());
        profile.setBudgetMin(request.getBudgetMin());
        profile.setBudgetMax(request.getBudgetMax());
        profile.setSleepSchedule(request.getSleepSchedule());
        profile.setCleanliness(request.getCleanliness());
        profile.setNoiseLevel(request.getNoiseLevel());
        profile.setPetsAllowed(request.isPetsAllowed());
        profile.setSmokingAllowed(request.isSmokingAllowed());
        profile.setGenderPreference(request.getGenderPreference());
        profile.setOccupation(request.getOccupation());
        profile.setAge(request.getAge());
        profile.setBio(request.getBio());
        profile.setLookingForRoom(request.isLookingForRoom());

        applyWeights(profile, request);

        Profile saved = profileRepository.save(profile);
        return toDTO(saved, null, null);
    }

    // ── Profile picture ─────────────────────────────────────────────────────

    @Transactional
    public String uploadProfilePicture(Long userId, MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/jpeg")
                && !contentType.startsWith("image/png")
                && !contentType.startsWith("image/webp"))) {
            throw new RuntimeException("Only JPEG, PNG, and WebP images are allowed");
        }

        String ext = contentType.contains("png") ? ".png" : contentType.contains("webp") ? ".webp" : ".jpg";
        String filename = "user_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        String url = "/uploads/profile-pictures/" + filename;

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Complete your profile before uploading a picture"));
        profile.setProfilePicture(url);
        profileRepository.save(profile);
        return url;
    }

    // ── Read ────────────────────────────────────────────────────────────────

    public ProfileDTO getProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return toDTO(profile, null, null);
    }

    public List<ProfileDTO> getMatches(Long userId) {
        Profile viewer = profileRepository.findByUserId(userId).orElse(null);
        List<Profile> candidates = viewer != null
                ? profileRepository.findByCityAndUserIdNot(viewer.getCity(), userId)
                : profileRepository.findAllExceptUser(userId);
        return ranked(viewer, candidates);
    }

    public List<ProfileDTO> searchProfiles(Long userId, String city,
                                            Integer budgetMin, Integer budgetMax,
                                            String sleepSchedule) {
        Profile viewer = profileRepository.findByUserId(userId).orElse(null);

        Profile.SleepSchedule schedule = null;
        if (sleepSchedule != null && !sleepSchedule.isBlank()) {
            try { schedule = Profile.SleepSchedule.valueOf(sleepSchedule); } catch (Exception ignored) {}
        }

        List<Profile> results = profileRepository.findWithFilters(
                userId,
                (city != null && !city.isBlank()) ? city.trim() : null,
                budgetMin, budgetMax, schedule
        );
        return ranked(viewer, results);
    }

    public Page<ProfileDTO> getMatchesPaged(Long userId, Pageable pageable) {
        Profile viewer = profileRepository.findByUserId(userId).orElse(null);
        List<Profile> candidates = viewer != null
                ? profileRepository.findByCityAndUserIdNot(viewer.getCity(), userId)
                : profileRepository.findAllExceptUser(userId);

        List<ProfileDTO> sorted = ranked(viewer, candidates);
        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), sorted.size());
        return new PageImpl<>(start >= sorted.size() ? List.of() : sorted.subList(start, end),
                              pageable, sorted.size());
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    private List<ProfileDTO> ranked(Profile viewer, List<Profile> candidates) {
        return candidates.stream()
                .map(c -> {
                    if (viewer == null) return toDTO(c, null, null);
                    int score = compatibilityService.calculateScore(viewer, c);
                    List<CompatibilityBreakdown> breakdown = compatibilityService.explain(viewer, c);
                    return toDTO(c, score, breakdown);
                })
                .sorted(Comparator.comparingInt(dto -> -dto.getCompatibilityScore()))
                .collect(Collectors.toList());
    }

    private int calculateCompleteness(Profile p) {
        @SuppressWarnings("unchecked")
        java.util.function.Supplier<Boolean>[] checks = new java.util.function.Supplier[]{
            () -> p.getCity() != null && !p.getCity().isBlank(),
            () -> p.getBudgetMin() != null,
            () -> p.getBudgetMax() != null,
            () -> p.getSleepSchedule() != null,
            () -> p.getCleanliness() != null,
            () -> p.getNoiseLevel() != null,
            () -> p.getGenderPreference() != null,
            () -> p.getAge() != null,
            () -> p.getOccupation() != null && !p.getOccupation().isBlank(),
            () -> p.getBio() != null && !p.getBio().isBlank(),
            () -> p.getPreferredArea() != null && !p.getPreferredArea().isBlank(),
            () -> p.getProfilePicture() != null && !p.getProfilePicture().isBlank()
        };
        int filled = 0;
        for (var check : checks) if ((boolean) check.get()) filled++;
        return (int) Math.round((double) filled / checks.length * 100);
    }

    private List<String> getMissingFields(Profile p) {
        List<String> missing = new ArrayList<>();
        if (p.getCity() == null || p.getCity().isBlank()) missing.add("City");
        if (p.getBudgetMin() == null || p.getBudgetMax() == null) missing.add("Budget range");
        if (p.getSleepSchedule() == null) missing.add("Sleep schedule");
        if (p.getCleanliness() == null) missing.add("Cleanliness level");
        if (p.getNoiseLevel() == null) missing.add("Noise level");
        if (p.getGenderPreference() == null) missing.add("Gender preference");
        if (p.getAge() == null) missing.add("Age");
        if (p.getOccupation() == null || p.getOccupation().isBlank()) missing.add("Occupation");
        if (p.getBio() == null || p.getBio().isBlank()) missing.add("Bio");
        if (p.getPreferredArea() == null || p.getPreferredArea().isBlank()) missing.add("Preferred area");
        if (p.getProfilePicture() == null || p.getProfilePicture().isBlank()) missing.add("Profile picture");
        return missing;
    }

    ProfileDTO toDTO(Profile p, Integer score, List<CompatibilityBreakdown> breakdown) {
        String quality = score != null ? compatibilityService.matchQuality(score) : null;
        return ProfileDTO.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .userName(p.getUser().getName())
                .userEmail(p.getUser().getEmail())
                .city(p.getCity())
                .preferredArea(p.getPreferredArea())
                .budgetMin(p.getBudgetMin())
                .budgetMax(p.getBudgetMax())
                .sleepSchedule(p.getSleepSchedule() != null ? p.getSleepSchedule().name() : null)
                .cleanliness(p.getCleanliness() != null ? p.getCleanliness().name() : null)
                .noiseLevel(p.getNoiseLevel() != null ? p.getNoiseLevel().name() : null)
                .petsAllowed(p.isPetsAllowed())
                .smokingAllowed(p.isSmokingAllowed())
                .genderPreference(p.getGenderPreference() != null ? p.getGenderPreference().name() : null)
                .occupation(p.getOccupation())
                .age(p.getAge())
                .bio(p.getBio())
                .lookingForRoom(p.isLookingForRoom())
                .profilePicture(p.getProfilePicture())
                .compatibilityScore(score != null ? score : 0)
                .matchQuality(quality)
                .breakdown(breakdown)
                .whyMatched(buildWhyMatched(breakdown))
                .matchSummary(breakdown != null && score != null ? matchSummaryService.generate(p.getUser().getName(), score, breakdown) : null)
                .completenessScore(calculateCompleteness(p))
                .missingFields(getMissingFields(p))
                // expose viewer's own weights on every DTO (used by profile page)
                .budgetWeight(p.getBudgetWeight())
                .sleepWeight(p.getSleepWeight())
                .cleanlinessWeight(p.getCleanlinessWeight())
                .noiseWeight(p.getNoiseWeight())
                .petsWeight(p.getPetsWeight())
                .smokingWeight(p.getSmokingWeight())
                .build();
    }

    private List<String> buildWhyMatched(List<CompatibilityBreakdown> breakdown) {
        if (breakdown == null) return null;
        List<String> sentences = new ArrayList<>();
        for (CompatibilityBreakdown b : breakdown) {
            String icon = switch (b.getStatus()) {
                case "MATCH"    -> "✓";
                case "PARTIAL"  -> "⚠";
                default         -> "✗";
            };
            sentences.add(icon + " " + b.getReason());
        }
        return sentences;
    }

}
