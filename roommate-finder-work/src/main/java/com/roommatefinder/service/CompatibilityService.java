package com.roommatefinder.service;

import com.roommatefinder.dto.CompatibilityBreakdown;
import com.roommatefinder.entity.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Personalized compatibility scoring engine.
 *
 * Weights are taken from the viewer's (p1's) stored preferences, so every
 * user gets a score calculated according to what *they* care about most.
 *
 * The comparison logic (budget overlap, sleep schedule matching, etc.) is
 * unchanged from the original algorithm — only the point values are dynamic.
 */
@Service
public class CompatibilityService {

    // ── Public API ───────────────────────────────────────────────────────────

    /** Total score 0-100, using viewer's (p1's) weights. */
    public int calculateScore(Profile viewer, Profile candidate) {
        return buildBreakdown(viewer, candidate).stream()
                .mapToInt(CompatibilityBreakdown::getEarned)
                .sum();
    }

    /** Full per-factor breakdown list. */
    public List<CompatibilityBreakdown> explain(Profile viewer, Profile candidate) {
        return buildBreakdown(viewer, candidate);
    }

    /** Human-readable quality label for a given score. */
    public String matchQuality(int score) {
        if (score >= 95) return "Perfect Match";
        if (score >= 85) return "Excellent Match";
        if (score >= 70) return "Good Match";
        if (score >= 55) return "Fair Match";
        return "Low Match";
    }

    // ── Breakdown builder ────────────────────────────────────────────────────

    private List<CompatibilityBreakdown> buildBreakdown(Profile v, Profile c) {
        return List.of(
            budget(v, c),
            sleep(v, c),
            cleanliness(v, c),
            noise(v, c),
            pets(v, c),
            smoking(v, c)
        );
    }

    // ── Factor scorers ───────────────────────────────────────────────────────

    private CompatibilityBreakdown budget(Profile v, Profile c) {
        int max = v.getBudgetWeight();
        int low  = Math.max(v.getBudgetMin(), c.getBudgetMin());
        int high = Math.min(v.getBudgetMax(), c.getBudgetMax());

        if (low <= high) {
            return bd("Budget", max, max, "MATCH", "Budget ranges overlap");
        }
        int gap = low - high;
        int avg = (v.getBudgetMax() + c.getBudgetMax()) / 2;
        if (gap <= avg * 0.10) {
            int earned = (int) Math.round(max * 0.67);
            return bd("Budget", earned, max, "PARTIAL", "Budget gap within 10%");
        }
        if (gap <= avg * 0.20) {
            int earned = (int) Math.round(max * 0.33);
            return bd("Budget", earned, max, "PARTIAL", "Budget gap within 20%");
        }
        return bd("Budget", 0, max, "MISMATCH", "Budget ranges are too far apart");
    }

    private CompatibilityBreakdown sleep(Profile v, Profile c) {
        int max = v.getSleepWeight();
        if (v.getSleepSchedule() == null || c.getSleepSchedule() == null) {
            int partial = (int) Math.round(max * 0.5);
            return bd("Sleep Schedule", partial, max, "PARTIAL", "Sleep schedule not specified");
        }
        if (v.getSleepSchedule() == c.getSleepSchedule()) {
            return bd("Sleep Schedule", max, max, "MATCH", "Same sleep schedule");
        }
        boolean flexible = v.getSleepSchedule() == Profile.SleepSchedule.FLEXIBLE
                        || c.getSleepSchedule() == Profile.SleepSchedule.FLEXIBLE;
        if (flexible) {
            int partial = (int) Math.round(max * 0.6);
            return bd("Sleep Schedule", partial, max, "PARTIAL", "One person is flexible");
        }
        return bd("Sleep Schedule", 0, max, "MISMATCH", "Different sleep schedules");
    }

    private CompatibilityBreakdown cleanliness(Profile v, Profile c) {
        int max = v.getCleanlinessWeight();
        if (v.getCleanliness() == null || c.getCleanliness() == null) {
            int partial = (int) Math.round(max * 0.5);
            return bd("Cleanliness", partial, max, "PARTIAL", "Cleanliness not specified");
        }
        if (v.getCleanliness() == c.getCleanliness()) {
            return bd("Cleanliness", max, max, "MATCH", "Same cleanliness standards");
        }
        int diff = Math.abs(v.getCleanliness().ordinal() - c.getCleanliness().ordinal());
        if (diff == 1) {
            int partial = (int) Math.round(max * 0.5);
            return bd("Cleanliness", partial, max, "PARTIAL", "Slightly different cleanliness habits");
        }
        return bd("Cleanliness", 0, max, "MISMATCH", "Very different cleanliness standards");
    }

    private CompatibilityBreakdown noise(Profile v, Profile c) {
        int max = v.getNoiseWeight();
        if (v.getNoiseLevel() == null || c.getNoiseLevel() == null) {
            int partial = (int) Math.round(max * 0.53);
            return bd("Noise Level", partial, max, "PARTIAL", "Noise preference not specified");
        }
        if (v.getNoiseLevel() == c.getNoiseLevel()) {
            return bd("Noise Level", max, max, "MATCH", "Same noise preference");
        }
        int diff = Math.abs(v.getNoiseLevel().ordinal() - c.getNoiseLevel().ordinal());
        if (diff == 1) {
            int partial = (int) Math.round(max * 0.53);
            return bd("Noise Level", partial, max, "PARTIAL", "Slightly different noise preferences");
        }
        return bd("Noise Level", 0, max, "MISMATCH", "Very different noise preferences");
    }

    private CompatibilityBreakdown pets(Profile v, Profile c) {
        int max = v.getPetsWeight();
        if (v.isPetsAllowed() == c.isPetsAllowed()) {
            return bd("Pets", max, max, "MATCH",
                v.isPetsAllowed() ? "Both comfortable with pets" : "Neither wants pets");
        }
        return bd("Pets", 0, max, "MISMATCH", "Different pet preferences");
    }

    private CompatibilityBreakdown smoking(Profile v, Profile c) {
        int max = v.getSmokingWeight();
        if (v.isSmokingAllowed() == c.isSmokingAllowed()) {
            return bd("Smoking", max, max, "MATCH",
                v.isSmokingAllowed() ? "Both OK with smoking" : "Neither smokes");
        }
        return bd("Smoking", 0, max, "MISMATCH", "Different smoking preferences");
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private CompatibilityBreakdown bd(String label, int earned, int max, String status, String reason) {
        return CompatibilityBreakdown.builder()
                .label(label).earned(earned).max(max).status(status).reason(reason)
                .build();
    }
}
