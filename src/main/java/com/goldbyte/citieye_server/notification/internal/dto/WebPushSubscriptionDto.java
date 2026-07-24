package com.goldbyte.citieye_server.notification.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class WebPushSubscriptionDto {
    @NotBlank(message = "Endpoint is required")
    private String endpoint;

    @NotNull(message = "Keys are required")
    private KeysDto keys;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public KeysDto getKeys() {
        return keys;
    }

    public void setKeys(KeysDto keys) {
        this.keys = keys;
    }

    public static class KeysDto {
        @NotBlank(message = "p256dh is required")
        private String p256dh;

        @NotBlank(message = "auth is required")
        private String auth;

        public String getP256dh() {
            return p256dh;
        }

        public void setP256dh(String p256dh) {
            this.p256dh = p256dh;
        }

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }
    }
}
