package com.roommatefinder.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversationDTO {
    private Long matchRequestId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserPicture;   // may be null → show initials
    private int    compatibilityScore;
    private String matchQuality;
    private String lastMessage;        // null if no messages yet
    private LocalDateTime lastMessageTime;
    private int    unreadCount;
}
