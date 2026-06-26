package com.roommatefinder.service;

import com.roommatefinder.dto.MatchRequestDTO;
import com.roommatefinder.entity.MatchRequest;
import com.roommatefinder.entity.Profile;
import com.roommatefinder.entity.User;
import com.roommatefinder.repository.MatchRequestRepository;
import com.roommatefinder.repository.ProfileRepository;
import com.roommatefinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRequestRepository matchRequestRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CompatibilityService compatibilityService;
    private final EmailService emailService;

    @Transactional
    public String sendMatchRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("Cannot send request to yourself");
        }

        if (matchRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId).isPresent()) {
            throw new RuntimeException("Match request already sent");
        }

        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("Receiver not found"));

        Profile senderProfile = profileRepository.findByUserId(senderId)
                .orElseThrow(() -> new RuntimeException("Please complete your profile before sending requests"));
        Profile receiverProfile = profileRepository.findByUserId(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver has no profile"));

        int score = compatibilityService.calculateScore(senderProfile, receiverProfile);

        MatchRequest request = MatchRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(MatchRequest.Status.PENDING)
                .compatibilityScore(score)
                .build();

        matchRequestRepository.save(request);
        emailService.sendMatchRequestEmail(receiver.getEmail(), receiver.getName(), sender.getName(), score);

        return "Match request sent! Compatibility score: " + score + "%";
    }

    @Transactional
    public String respondToRequest(Long requestId, Long userId, boolean accept) {
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to respond to this request");
        }

        if (request.getStatus() != MatchRequest.Status.PENDING) {
            throw new RuntimeException("Request already responded to");
        }

        request.setStatus(accept ? MatchRequest.Status.ACCEPTED : MatchRequest.Status.REJECTED);
        request.setRespondedAt(LocalDateTime.now());
        matchRequestRepository.save(request);

        if (accept) {
            emailService.sendMatchAcceptedEmail(
                    request.getSender().getEmail(),
                    request.getSender().getName(),
                    request.getReceiver().getName()
            );
            return "Match accepted! " + request.getSender().getName() + " has been notified.";
        }

        return "Request declined.";
    }

    /**
     * Withdraw a pending match request sent by the current user.
     * Only the original sender may withdraw, and only while it is still PENDING.
     */
    @Transactional
    public String withdrawMatchRequest(Long requestId, Long senderId) {
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Match request not found"));

        if (!request.getSender().getId().equals(senderId)) {
            throw new RuntimeException("Not authorized to withdraw this request");
        }

        if (request.getStatus() != MatchRequest.Status.PENDING) {
            throw new RuntimeException("Only pending requests can be withdrawn");
        }

        matchRequestRepository.delete(request);
        return "Match request withdrawn successfully";
    }

    public List<MatchRequestDTO> getSentRequests(Long userId) {
        return matchRequestRepository.findBySenderIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MatchRequestDTO> getReceivedRequests(Long userId) {
        return matchRequestRepository.findByReceiverIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MatchRequestDTO> getAcceptedMatches(Long userId) {
        return matchRequestRepository.findAcceptedMatchesByUserId(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private MatchRequestDTO toDTO(MatchRequest m) {
        return MatchRequestDTO.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .senderEmail(m.getSender().getEmail())
                .receiverId(m.getReceiver().getId())
                .receiverName(m.getReceiver().getName())
                .receiverEmail(m.getReceiver().getEmail())
                .status(m.getStatus().name())
                .compatibilityScore(m.getCompatibilityScore())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
