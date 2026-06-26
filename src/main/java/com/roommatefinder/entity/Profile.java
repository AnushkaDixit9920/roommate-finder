package com.roommatefinder.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Location
    @Column(nullable = false)
    private String city;

    private String preferredArea;

    // Budget
    @Column(nullable = false)
    private Integer budgetMin;

    @Column(nullable = false)
    private Integer budgetMax;

    // Lifestyle preferences (used in matching algorithm)
    @Enumerated(EnumType.STRING)
    private SleepSchedule sleepSchedule;

    @Enumerated(EnumType.STRING)
    private CleanlinessLevel cleanliness;

    @Enumerated(EnumType.STRING)
    private NoiseLevel noiseLevel;

    private boolean petsAllowed;
    private boolean smokingAllowed;

    @Enumerated(EnumType.STRING)
    private GenderPreference genderPreference;

    // Personal info
    private String occupation;
    private Integer age;

    @Column(length = 500)
    private String bio;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String profilePicture;

    private boolean lookingForRoom = true;

    // --- Personalized matching weights (default = legacy fixed weights, must total 100) ---
    @Column(nullable = false)
    @Builder.Default
    private int budgetWeight = 30;

    @Column(nullable = false)
    @Builder.Default
    private int sleepWeight = 20;

    @Column(nullable = false)
    @Builder.Default
    private int cleanlinessWeight = 20;

    @Column(nullable = false)
    @Builder.Default
    private int noiseWeight = 15;

    @Column(nullable = false)
    @Builder.Default
    private int petsWeight = 10;

    @Column(nullable = false)
    @Builder.Default
    private int smokingWeight = 5;

    public enum SleepSchedule { EARLY_BIRD, NIGHT_OWL, FLEXIBLE }
    public enum CleanlinessLevel { VERY_CLEAN, MODERATE, RELAXED }
    public enum NoiseLevel { QUIET, MODERATE, SOCIAL }
    public enum GenderPreference { MALE, FEMALE, ANY }
}
