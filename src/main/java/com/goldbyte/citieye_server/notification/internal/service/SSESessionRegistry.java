package com.goldbyte.citieye_server.notification.internal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe registry that stores active Server-Sent Events (SSE) connections 
 * for logged-in citizens to enable real-time targeted updates.
 */
@Service
public class SSESessionRegistry {
    private static final Logger log = LoggerFactory.getLogger(SSESessionRegistry.class);

    // Mappings: citizen_code -> List of active SseEmitters (supports multiple browser tabs)
    private final Map<String, List<SseEmitter>> registry = new ConcurrentHashMap<>();

    public void register(String citizenCode, SseEmitter emitter) {
        registry.computeIfAbsent(citizenCode, k -> new CopyOnWriteArrayList<>()).add(emitter);
        log.info("Registered SSE connection for citizen: {} (Active sessions: {})", 
                citizenCode, registry.get(citizenCode).size());

        emitter.onCompletion(() -> unregister(citizenCode, emitter));
        emitter.onTimeout(() -> {
            log.debug("SSE timeout for citizen: {}", citizenCode);
            emitter.complete();
            unregister(citizenCode, emitter);
        });
        emitter.onError(ex -> {
            log.debug("SSE error for citizen: {}: {}", citizenCode, ex.getMessage());
            emitter.complete();
            unregister(citizenCode, emitter);
        });
    }

    public void unregister(String citizenCode, SseEmitter emitter) {
        List<SseEmitter> emitters = registry.get(citizenCode);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                registry.remove(citizenCode);
                log.info("Unregistered all SSE connections for citizen: {}", citizenCode);
            } else {
                log.info("Unregistered one SSE connection for citizen: {} (Remaining sessions: {})", 
                        citizenCode, emitters.size());
            }
        }
    }

    public List<SseEmitter> getEmittersFor(String citizenCode) {
        List<SseEmitter> emitters = registry.get(citizenCode);
        return emitters != null ? new ArrayList<>(emitters) : List.of();
    }

    /**
     * Periodic 15-second heartbeat ping to prevent HTTP socket timeouts and proxy disconnects.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 15000)
    public void sendHeartbeat() {
        if (registry.isEmpty()) return;
        registry.forEach((citizenCode, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (Exception e) {
                    log.debug("Heartbeat failed for citizen {}, unregistering emitter", citizenCode);
                    unregister(citizenCode, emitter);
                }
            }
        });
    }
}
