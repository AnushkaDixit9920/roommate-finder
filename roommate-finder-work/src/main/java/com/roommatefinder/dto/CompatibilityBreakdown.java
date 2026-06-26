package com.roommatefinder.dto;

import lombok.*;

/**
 * Per-factor breakdown of a compatibility score.
 * earned  = points awarded for this factor
 * max     = maximum possible points for this factor (= viewer's weight)
 * label   = human-readable factor name ("Budget", "Sleep Schedule", …)
 * status  = MATCH | PARTIAL | MISMATCH — drives the ✓/⚠/✗ icon
 * reason  = one-line explanation ("Same sleep schedule", "Budget gap > 20%", …)
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompatibilityBreakdown {
    private String label;
    private int earned;
    private int max;
    private String status;  // MATCH | PARTIAL | MISMATCH
    private String reason;
}
