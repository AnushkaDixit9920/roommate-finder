package com.roommatefinder.dto;

import com.roommatefinder.entity.Profile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileRequest {
    @NotBlank
    private String city;

    private String preferredArea;

    @NotNull
    private Integer budgetMin;

    @NotNull
    private Integer budgetMax;

    private Profile.SleepSchedule sleepSchedule;
    private Profile.CleanlinessLevel cleanliness;
    private Profile.NoiseLevel noiseLevel;

    private boolean petsAllowed;
    private boolean smokingAllowed;

    private Profile.GenderPreference genderPreference;

    private String occupation;
    private Integer age;
    private String bio;
    private boolean lookingForRoom = true;

    // Personalized weights — optional; when omitted, existing values are preserved
    private Integer budgetWeight;
    private Integer sleepWeight;
    private Integer cleanlinessWeight;
    private Integer noiseWeight;
    private Integer petsWeight;
    private Integer smokingWeight;
}
