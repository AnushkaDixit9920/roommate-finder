package com.roommatefinder.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String city;
    private String preferredArea;
    private Integer budgetMin;
    private Integer budgetMax;
    private String sleepSchedule;
    private String cleanliness;
    private String noiseLevel;
    private boolean petsAllowed;
    private boolean smokingAllowed;
    private String genderPreference;
    private String occupation;
    private Integer age;
    private String bio;
    private String profilePicture;
    private boolean lookingForRoom;

    // Completeness
    private int completenessScore;
    private List<String> missingFields;

    // Compatibility
    private int compatibilityScore;
    private String matchQuality;               // "Perfect Match", "Excellent Match", etc.
    private List<CompatibilityBreakdown> breakdown; // per-factor detail
    private List<String> whyMatched;             // clean sentence list
    private String matchSummary;                 // natural-language AI-free summary

    // Viewer's own weights (only populated on /me responses)
    private int budgetWeight;
    private int sleepWeight;
    private int cleanlinessWeight;
    private int noiseWeight;
    private int petsWeight;
    private int smokingWeight;
}
