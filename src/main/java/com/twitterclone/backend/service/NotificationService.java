package com.twitterclone.backend.service;

import com.twitterclone.backend.dto.NotificationSseDto;
import com.twitterclone.backend.model.Notification;
import com.twitterclone.backend.model.NotificationType;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    @Transactional
    public Notification createNotification(User receiver, User actor, NotificationType type, UUID referenceId) {
        // Do not notify a user about their own actions
        if (receiver.getId().equals(actor.getId())) {
            return null;
        }

        Notification notification = Notification.builder()
                .receiver(receiver)
                .actor(actor)
                .type(type)
                .referenceId(referenceId)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Dispatch over Server-Sent Events (SSE)
        NotificationSseDto dto = NotificationSseDto.builder()
                .id(saved.getId())
                .actorUsername(actor.getUsername())
                .type(type.name())
                .referenceId(referenceId)
                .createdAt(saved.getCreatedAt())
                .build();

        sseService.sendNotification(receiver.getId(), dto);

        return saved;
    }
}
