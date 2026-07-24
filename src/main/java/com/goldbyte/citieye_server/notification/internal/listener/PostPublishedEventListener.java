package com.goldbyte.citieye_server.notification.internal.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldbyte.citieye_server.admin_content_cms.PostPublishedEvent;
import com.goldbyte.citieye_server.notification.QueryTargetUsersEvent;
import com.goldbyte.citieye_server.notification.internal.entity.WebPushSubscriptionEntity;
import com.goldbyte.citieye_server.notification.internal.repo.WebPushSubscriptionRepository;
import com.goldbyte.citieye_server.notification.internal.service.SSESessionRegistry;
import com.goldbyte.citieye_server.notification.internal.service.WebPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PostPublishedEventListener {
    private static final Logger log = LoggerFactory.getLogger(PostPublishedEventListener.class);

    private final ApplicationEventPublisher eventPublisher;
    private final SSESessionRegistry sseSessionRegistry;
    private final WebPushSubscriptionRepository subscriptionRepository;
    private final WebPushService webPushService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PostPublishedEventListener(ApplicationEventPublisher eventPublisher, 
                                      SSESessionRegistry sseSessionRegistry, 
                                      WebPushSubscriptionRepository subscriptionRepository, 
                                      WebPushService webPushService) {
        this.eventPublisher = eventPublisher;
        this.sseSessionRegistry = sseSessionRegistry;
        this.subscriptionRepository = subscriptionRepository;
        this.webPushService = webPushService;
    }

    /**
     * Listens to PostPublishedEvent asynchronously AFTER the transaction has successfully committed.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostPublishedEvent(PostPublishedEvent event) {
        log.info("Processing post publication event asynchronously for post: {}", event.getPostId());

        // 1. Query target users' citizen codes
        QueryTargetUsersEvent queryEvent = new QueryTargetUsersEvent(
                event.getTargetCohortId(),
                event.getTargetStateResidence(),
                event.getTargetStateOrigin(),
                event.getTargetDesiredSupport()
        );
        eventPublisher.publishEvent(queryEvent);
        List<String> targetCitizenCodes = queryEvent.getTargetCitizenCodes();
        log.info("Target users matched for post: count={}", targetCitizenCodes.size());

        if (targetCitizenCodes.isEmpty()) {
            return;
        }

        // 2. Prepare payload — includes all fields the citizen dashboard PostDto exposes
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("id", event.getPostId().toString());
        payloadMap.put("postId", event.getPostId().toString());
        payloadMap.put("title", event.getTitle());
        payloadMap.put("content", event.getContent());
        payloadMap.put("author", event.getAuthor());
        payloadMap.put("contentCategory", event.getContentCategory());
        payloadMap.put("datePublished", event.getDatePublished() != null ? event.getDatePublished().toString() : null);
        payloadMap.put("pictureUrl", event.getPictureUrl());
        payloadMap.put("targetCohortId", event.getTargetCohortId());
        payloadMap.put("targetStateResidence", event.getTargetStateResidence());
        payloadMap.put("targetStateOrigin", event.getTargetStateOrigin());
        payloadMap.put("targetDesiredSupport", event.getTargetDesiredSupport());

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            log.error("Failed to serialize post payload", e);
            return;
        }

        // 3. Dispatch to online users via SSE
        int sseCount = 0;
        for (String citizenCode : targetCitizenCodes) {
            List<SseEmitter> emitters = sseSessionRegistry.getEmittersFor(citizenCode);
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("new-post")
                            .data(jsonPayload));
                    sseCount++;
                } catch (IOException e) {
                    log.debug("Failed to deliver SSE notification to user {}: {}", citizenCode, e.getMessage());
                    sseSessionRegistry.unregister(citizenCode, emitter);
                }
            }
        }
        log.info("Delivered real-time SSE notifications to {} active tabs.", sseCount);

        // 4. Dispatch to users via Web Push (whether online or offline)
        List<WebPushSubscriptionEntity> subscriptions = subscriptionRepository.findByCitizenCodeIn(targetCitizenCodes);
        int pushCount = 0;
        for (WebPushSubscriptionEntity sub : subscriptions) {
            boolean active = webPushService.sendPushNotification(
                    sub.getEndpoint(),
                    sub.getP256dh(),
                    sub.getAuth(),
                    jsonPayload
            );
            if (!active) {
                subscriptionRepository.delete(sub);
                log.info("Removed expired subscription: {}", sub.getEndpoint());
            } else {
                pushCount++;
            }
        }
        log.info("Dispatched Web Push notifications to {} subscription endpoints.", pushCount);
    }
}
