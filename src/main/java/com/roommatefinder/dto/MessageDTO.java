package com.roommatefinder.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageDTO {
    private Long id;
    private Long matchRequestId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private boolean readStatus;
    private LocalDateTime timestamp;
}
