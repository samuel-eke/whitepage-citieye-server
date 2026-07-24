package com.goldbyte.citieye_server.notification.internal.controller;

import com.goldbyte.citieye_server.notification.internal.dto.WebPushSubscriptionDto;
import com.goldbyte.citieye_server.notification.internal.entity.WebPushSubscriptionEntity;
import com.goldbyte.citieye_server.notification.internal.repo.WebPushSubscriptionRepository;
import com.goldbyte.citieye_server.notification.internal.service.WebPushService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/notifications")
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final WebPushSubscriptionRepository subscriptionRepository;
    private final WebPushService webPushService;

    public NotificationController(WebPushSubscriptionRepository subscriptionRepository, 
                                  WebPushService webPushService) {
        this.subscriptionRepository = subscriptionRepository;
        this.webPushService = webPushService;
    }

    @GetMapping("/vapid-public-key")
    public ResponseEntity<Map<String, String>> getVapidPublicKey() {
        String key = webPushService.getVapidPublicKey();
        if (key == null || key.trim().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("publicKey", key));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@Valid @RequestBody WebPushSubscriptionDto subscriptionDto) {
        String citizenCode = SecurityContextHolder.getContext().getAuthentication().getName();
        if (citizenCode == null || citizenCode.equals("anonymousUser")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access."));
        }

        Optional<WebPushSubscriptionEntity> existingOpt = 
                subscriptionRepository.findFirstByEndpoint(subscriptionDto.getEndpoint());

        WebPushSubscriptionEntity entity;
        if (existingOpt.isPresent()) {
            entity = existingOpt.get();
            entity.setCitizenCode(citizenCode);
            entity.setP256dh(subscriptionDto.getKeys().getP256dh());
            entity.setAuth(subscriptionDto.getKeys().getAuth());
            log.info("Updating existing Web Push subscription for citizen: {}", citizenCode);
        } else {
            entity = new WebPushSubscriptionEntity();
            entity.setCitizenCode(citizenCode);
            entity.setEndpoint(subscriptionDto.getEndpoint());
            entity.setP256dh(subscriptionDto.getKeys().getP256dh());
            entity.setAuth(subscriptionDto.getKeys().getAuth());
            log.info("Creating new Web Push subscription for citizen: {}", citizenCode);
        }

        subscriptionRepository.save(entity);
        return ResponseEntity.ok(Map.of("message", "Subscription registered successfully."));
    }
}
