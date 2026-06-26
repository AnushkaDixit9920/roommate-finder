package com.roommatefinder.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private Integer compatibilityScore;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime respondedAt;

    public enum Status { PENDING, ACCEPTED, REJECTED }
}
