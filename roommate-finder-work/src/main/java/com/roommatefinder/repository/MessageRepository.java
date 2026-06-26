package com.roommatefinder.repository;

import com.roommatefinder.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /** All messages for a conversation, oldest first. */
    List<Message> findByMatchRequestIdOrderByTimestampAsc(Long matchRequestId);

    /** Count unread messages sent to a specific receiver in a conversation. */
    int countByMatchRequestIdAndReceiverIdAndReadStatusFalse(Long matchRequestId, Long receiverId);

    /** Total unread across ALL conversations for a user. */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.readStatus = false")
    int countAllUnreadForUser(@Param("userId") Long userId);

    /** Last message in a conversation (for the inbox preview). */
    Optional<Message> findTopByMatchRequestIdOrderByTimestampDesc(Long matchRequestId);

    /** Mark all messages in a conversation as read for a given receiver. */
    @Modifying
    @Query("UPDATE Message m SET m.readStatus = true WHERE m.matchRequest.id = :matchRequestId AND m.receiver.id = :receiverId")
    void markAllReadInConversation(@Param("matchRequestId") Long matchRequestId,
                                   @Param("receiverId") Long receiverId);
}
