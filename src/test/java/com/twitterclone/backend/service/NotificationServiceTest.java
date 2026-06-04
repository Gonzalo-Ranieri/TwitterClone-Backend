package com.twitterclone.backend.service;

import com.twitterclone.backend.dto.NotificationSseDto;
import com.twitterclone.backend.model.Notification;
import com.twitterclone.backend.model.NotificationType;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SseService sseService;

    @InjectMocks
    private NotificationService notificationService;

    private User receiver;
    private User actor;
    private UUID referenceId;

    @BeforeEach
    void setUp() {
        receiver = User.builder()
                .id(UUID.randomUUID())
                .username("receiverUser")
                .build();

        actor = User.builder()
                .id(UUID.randomUUID())
                .username("actorUser")
                .build();

        referenceId = UUID.randomUUID();
    }

    @Test
    void createNotification_whenValid_shouldSaveAndDispatch() {
        Notification expectedNotification = Notification.builder()
                .id(UUID.randomUUID())
                .receiver(receiver)
                .actor(actor)
                .type(NotificationType.LIKE)
                .referenceId(referenceId)
                .isRead(false)
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(expectedNotification);

        Notification result = notificationService.createNotification(receiver, actor, NotificationType.LIKE, referenceId);

        assertNotNull(result);
        assertEquals(expectedNotification.getId(), result.getId());
        assertEquals(receiver, result.getReceiver());
        assertEquals(actor, result.getActor());
        assertEquals(NotificationType.LIKE, result.getType());
        assertEquals(referenceId, result.getReferenceId());

        verify(notificationRepository, times(1)).save(any(Notification.class));

        ArgumentCaptor<NotificationSseDto> sseDtoCaptor = ArgumentCaptor.forClass(NotificationSseDto.class);
        verify(sseService, times(1)).sendNotification(eq(receiver.getId()), sseDtoCaptor.capture());

        NotificationSseDto capturedDto = sseDtoCaptor.getValue();
        assertEquals(expectedNotification.getId(), capturedDto.getId());
        assertEquals(actor.getUsername(), capturedDto.getActorUsername());
        assertEquals("LIKE", capturedDto.getType());
        assertEquals(referenceId, capturedDto.getReferenceId());
    }

    @Test
    void createNotification_whenSelfAction_shouldNotSaveOrDispatch() {
        // Actor is also the receiver
        Notification result = notificationService.createNotification(receiver, receiver, NotificationType.LIKE, referenceId);

        assertNull(result);
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(sseService, never()).sendNotification(any(UUID.class), any());
    }
}
