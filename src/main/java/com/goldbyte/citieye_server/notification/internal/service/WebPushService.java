package com.goldbyte.citieye_server.notification.internal.service;

import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Security;

@Service
public class WebPushService {
    private static final Logger log = LoggerFactory.getLogger(WebPushService.class);

    @Value("${citieye.vapid.public-key:}")
    private String vapidPublicKey;

    @Value("${citieye.vapid.private-key:}")
    private String vapidPrivateKey;

    @Value("${citieye.vapid.subject:mailto:admin@citieye.gov.ng}")
    private String vapidSubject;

    private PushService pushService;

    @PostConstruct
    public void init() {
        // Register Bouncy Castle security provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        if (vapidPublicKey != null && !vapidPublicKey.trim().isEmpty() &&
            vapidPrivateKey != null && !vapidPrivateKey.trim().isEmpty()) {
            try {
                pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
                log.info("Web Push Service initialized successfully with VAPID configuration.");
            } catch (Exception e) {
                log.error("Failed to initialize Web Push service: ", e);
            }
        } else {
            log.warn("Web Push VAPID keys are not configured. Offline notifications will be disabled.");
        }
    }

    public String getVapidPublicKey() {
        return vapidPublicKey;
    }

    /**
     * Sends a web push notification to a browser subscription.
     * @return true if sent successfully, false if the subscription is expired/invalid and should be deleted.
     */
    public boolean sendPushNotification(String endpoint, String p256dh, String auth, String payload) {
        if (pushService == null) {
            log.warn("Web Push Service is not configured. Skipping push message to: {}", endpoint);
            return true;
        }

        try {
            Subscription subscription = new Subscription();
            subscription.endpoint = endpoint;
            subscription.keys = new Subscription.Keys(p256dh, auth);

            Notification notification = new Notification(subscription, payload);
            HttpResponse response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 201) {
                log.debug("Push notification delivered to endpoint: {}", endpoint);
                return true;
            } else if (statusCode == 410 || statusCode == 404) {
                log.warn("Push subscription has expired or is invalid (HTTP {}). Endpoint: {}", statusCode, endpoint);
                return false; // Signals that this subscription should be deleted
            } else {
                log.warn("Push service returned HTTP error status: {} for endpoint: {}", statusCode, endpoint);
                return true;
            }
        } catch (Exception e) {
            log.error("Failed to send push notification to " + endpoint, e);
            return true;
        }
    }
}
