package com.roommatefinder.repository;

import com.roommatefinder.entity.MatchRequest;
import com.roommatefinder.entity.MatchRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    List<MatchRequest> findBySenderIdOrderByCreatedAtDesc(Long senderId);

    List<MatchRequest> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    List<MatchRequest> findByReceiverIdAndStatus(Long receiverId, Status status);

    Optional<MatchRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    @Query("SELECT m FROM MatchRequest m WHERE (m.sender.id = :userId OR m.receiver.id = :userId) AND m.status = 'ACCEPTED'")
    List<MatchRequest> findAcceptedMatchesByUserId(@Param("userId") Long userId);
}
