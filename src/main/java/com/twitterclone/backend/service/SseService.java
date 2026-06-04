package com.twitterclone.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseService {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(UUID userId) {
        // 5 minutes timeout (300,000 ms)
        SseEmitter emitter = new SseEmitter(300_000L);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE emitter completed for user: {}", userId);
            emitters.remove(userId, emitter);
        });

        emitter.onTimeout(() -> {
            log.info("SSE emitter timed out for user: {}", userId);
            emitter.complete();
            emitters.remove(userId, emitter);
        });

        emitter.onError((ex) -> {
            log.error("SSE emitter error for user: {}", userId, ex);
            emitter.completeWithError(ex);
            emitters.remove(userId, emitter);
        });

        // Send an initial handshake event to establish client connection successfully
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Connected"));
        } catch (IOException e) {
            log.error("Error sending initial event to user: {}", userId, e);
            emitters.remove(userId, emitter);
        }

        return emitter;
    }

    public void sendNotification(UUID userId, Object payload) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("NOTIFICATION")
                        .data(payload));
            } catch (IOException e) {
                log.error("Failed to send notification over SSE to user: {}", userId, e);
                emitter.completeWithError(e);
                emitters.remove(userId, emitter);
            }
        }
    }

    public SseEmitter getEmitter(UUID userId) {
        return emitters.get(userId);
    }
}
