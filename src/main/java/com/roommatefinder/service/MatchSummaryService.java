package com.roommatefinder.service;

import com.roommatefinder.dto.CompatibilityBreakdown;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates a natural-language match summary from existing compatibility data.
 * No external AI or API is used — the text is assembled from the breakdown results.
 */
@Service
public class MatchSummaryService {

    /**
     * Build a one-to-two sentence summary for display above the breakdown modal.
     *
     * @param otherName  first name (or full name) of the other person
     * @param score      overall compatibility score 0-100
     * @param breakdown  per-factor breakdown already computed by CompatibilityService
     * @return a natural-sounding summary string
     */
    public String generate(String otherName, int score, List<CompatibilityBreakdown> breakdown) {
        if (breakdown == null || breakdown.isEmpty()) {
            return "Complete your profile for a full compatibility summary.";
        }

        List<CompatibilityBreakdown> matches   = byStatus(breakdown, "MATCH");
        List<CompatibilityBreakdown> partials  = byStatus(breakdown, "PARTIAL");
        List<CompatibilityBreakdown> mismatches= byStatus(breakdown, "MISMATCH");

        String first = buildFirstName(otherName);

        // ── High compatibility (≥ 85) ────────────────────────────────────
        if (score >= 85) {
            if (mismatches.isEmpty()) {
                return "You and " + first + " align on " + listLabels(matches) +
                       ", making this a highly recommended roommate match with very few compromises needed.";
            }
            String weakArea = listLabels(mismatches);
            return "You and " + first + " share " + listLabels(matches) +
                   ". Compatibility is only slightly reduced by differences in " + weakArea + ".";
        }

        // ── Good compatibility (70–84) ───────────────────────────────────
        if (score >= 70) {
            String strongArea = !matches.isEmpty() ? listLabels(matches)
                    : !partials.isEmpty() ? listLabels(partials) : "several factors";
            if (!mismatches.isEmpty()) {
                return "You and " + first + " have similar " + strongArea +
                       ", though your compatibility is reduced by differences in " + listLabels(mismatches) + ".";
            }
            return "You and " + first + " are reasonably compatible, sharing " + strongArea + ".";
        }

        // ── Fair compatibility (55–69) ───────────────────────────────────
        if (score >= 55) {
            String weak = !mismatches.isEmpty() ? listLabels(mismatches)
                    : "several lifestyle factors";
            String strong = !matches.isEmpty() ? " You do align on " + listLabels(matches) + "." : "";
            return "You and " + first + " have some differences in " + weak +
                   " that may require compromise." + strong;
        }

        // ── Low compatibility (< 55) ─────────────────────────────────────
        if (!mismatches.isEmpty() && mismatches.size() >= 3) {
            return "You and " + first + " have significant differences in " + listLabels(mismatches) +
                   ". This match may require substantial compromise on daily living habits.";
        }
        return "You and " + first + " have a low compatibility score, primarily due to differences in " +
               listLabels(!mismatches.isEmpty() ? mismatches : breakdown) + ".";
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<CompatibilityBreakdown> byStatus(List<CompatibilityBreakdown> bd, String status) {
        return bd.stream().filter(b -> status.equals(b.getStatus())).collect(Collectors.toList());
    }

    /** "Budget, Sleep Schedule and Cleanliness" */
    private String listLabels(List<CompatibilityBreakdown> items) {
        if (items.isEmpty()) return "various factors";
        List<String> labels = items.stream().map(CompatibilityBreakdown::getLabel).collect(Collectors.toList());
        if (labels.size() == 1) return labels.get(0);
        if (labels.size() == 2) return labels.get(0) + " and " + labels.get(1);
        return String.join(", ", labels.subList(0, labels.size() - 1)) + " and " + labels.get(labels.size() - 1);
    }

    /** Use the first word of the name for a friendlier tone. */
    private String buildFirstName(String name) {
        if (name == null || name.isBlank()) return "this person";
        return name.split("\\s+")[0];
    }
}
