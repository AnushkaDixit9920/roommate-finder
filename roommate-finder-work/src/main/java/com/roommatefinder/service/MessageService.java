package com.roommatefinder.service;

import com.roommatefinder.dto.ConversationDTO;
import com.roommatefinder.dto.MessageDTO;
import com.roommatefinder.entity.MatchRequest;
import com.roommatefinder.entity.Message;
import com.roommatefinder.entity.User;
import com.roommatefinder.repository.MatchRequestRepository;
import com.roommatefinder.repository.MessageRepository;
import com.roommatefinder.repository.ProfileRepository;
import com.roommatefinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CompatibilityService compatibilityService;

    // ── Send a message ───────────────────────────────────────────────────────

    @Transactional
    public MessageDTO sendMessage(Long matchRequestId, Long senderId, String content) {
        MatchRequest match = getVerifiedAcceptedMatch(matchRequestId, senderId);

        User sender   = match.getSender().getId().equals(senderId) ? match.getSender() : match.getReceiver();
        User receiver = match.getSender().getId().equals(senderId) ? match.getReceiver() : match.getSender();

        Message msg = Message.builder()
                .matchRequest(match)
                .sender(sender)
                .receiver(receiver)
                .content(content.trim())
                .readStatus(false)
                .build();

        return toDTO(messageRepository.save(msg));
    }

    // ── Get all messages in a conversation ───────────────────────────────────

    @Transactional
    public List<MessageDTO> getMessages(Long matchRequestId, Long userId) {
        getVerifiedAcceptedMatch(matchRequestId, userId); // security check
        messageRepository.markAllReadInConversation(matchRequestId, userId);
        return messageRepository
                .findByMatchRequestIdOrderByTimestampAsc(matchRequestId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── Conversation list (inbox) ────────────────────────────────────────────

    public List<ConversationDTO> getConversations(Long userId) {
        List<MatchRequest> accepted = matchRequestRepository.findAcceptedMatchesByUserId(userId);

        return accepted.stream().map(match -> {
            User other = match.getSender().getId().equals(userId) ? match.getReceiver() : match.getSender();

            Optional<Message> lastMsg = messageRepository
                    .findTopByMatchRequestIdOrderByTimestampDesc(match.getId());

            int unread = messageRepository
                    .countByMatchRequestIdAndReceiverIdAndReadStatusFalse(match.getId(), userId);

            String quality = compatibilityService.matchQuality(
                    match.getCompatibilityScore() != null ? match.getCompatibilityScore() : 0);

            String pic = profileRepository.findByUserId(other.getId())
                    .map(p -> p.getProfilePicture()).orElse(null);

            return ConversationDTO.builder()
                    .matchRequestId(match.getId())
                    .otherUserId(other.getId())
                    .otherUserName(other.getName())
                    .otherUserPicture(pic)
                    .compatibilityScore(match.getCompatibilityScore() != null ? match.getCompatibilityScore() : 0)
                    .matchQuality(quality)
                    .lastMessage(lastMsg.map(Message::getContent).orElse(null))
                    .lastMessageTime(lastMsg.map(Message::getTimestamp).orElse(null))
                    .unreadCount(unread)
                    .build();
        }).collect(Collectors.toList());
    }

    // ── Total unread badge count ─────────────────────────────────────────────

    public int getTotalUnread(Long userId) {
        return messageRepository.countAllUnreadForUser(userId);
    }

    // ── Security helper ──────────────────────────────────────────────────────

    /**
     * Returns the MatchRequest only if:
     * 1. It exists
     * 2. Its status is ACCEPTED
     * 3. The requesting user is one of the two participants
     */
    private MatchRequest getVerifiedAcceptedMatch(Long matchRequestId, Long userId) {
        MatchRequest match = matchRequestRepository.findById(matchRequestId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (match.getStatus() != MatchRequest.Status.ACCEPTED) {
            throw new RuntimeException("Chat is only available after a match is accepted");
        }

        boolean participant = match.getSender().getId().equals(userId)
                || match.getReceiver().getId().equals(userId);
        if (!participant) {
            throw new RuntimeException("Access denied: you are not part of this conversation");
        }

        return match;
    }

    // ── DTO mapper ───────────────────────────────────────────────────────────

    private MessageDTO toDTO(Message m) {
        return MessageDTO.builder()
                .id(m.getId())
                .matchRequestId(m.getMatchRequest().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .receiverId(m.getReceiver().getId())
                .receiverName(m.getReceiver().getName())
                .content(m.getContent())
                .readStatus(m.isReadStatus())
                .timestamp(m.getTimestamp())
                .build();
    }
}
