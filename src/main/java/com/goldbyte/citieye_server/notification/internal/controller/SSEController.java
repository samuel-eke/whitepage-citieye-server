package com.goldbyte.citieye_server.notification.internal.controller;

import com.goldbyte.citieye_server.notification.internal.service.SSESessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("api/posts")
public class SSEController {
    private static final Logger log = LoggerFactory.getLogger(SSEController.class);
    
    private final SSESessionRegistry sseSessionRegistry;

    public SSEController(SSESessionRegistry sseSessionRegistry) {
        this.sseSessionRegistry = sseSessionRegistry;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPosts() {
        String citizenCode = SecurityContextHolder.getContext().getAuthentication().getName();
        if (citizenCode == null || citizenCode.equals("anonymousUser")) {
            throw new SecurityException("Unauthorized SSE request.");
        }

        // SseEmitter with no hard timeout (0L = indefinite).
        // The 15-second heartbeat in SSESessionRegistry drives lifecycle:
        // if the client disconnects, the next heartbeat send() throws IOException,
        // which triggers unregister(). spring.mvc.async.request-timeout=-1 ensures
        // Tomcat does not kill this async request out from under us.
        SseEmitter emitter = new SseEmitter(0L);
        
        sseSessionRegistry.register(citizenCode, emitter);

        // Send a handshake event to immediately verify connection success
        try {
            emitter.send(SseEmitter.event()
                    .name("handshake")
                    .data("connected"));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE handshake to citizen: {}", citizenCode);
            emitter.complete();
            sseSessionRegistry.unregister(citizenCode, emitter);
        }

        return emitter;
    }
}
