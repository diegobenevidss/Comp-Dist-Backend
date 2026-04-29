package com.healthsys.triage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.messaging")
public class MessagingProperties {

    private String exchange = "healthsys.events";
    private String authLogoutQueue = "healthsys.triage.auth-token-revoked";
    private String authLogoutRoutingKey = "healthsys.auth.token-revoked";

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getAuthLogoutQueue() {
        return authLogoutQueue;
    }

    public void setAuthLogoutQueue(String authLogoutQueue) {
        this.authLogoutQueue = authLogoutQueue;
    }

    public String getAuthLogoutRoutingKey() {
        return authLogoutRoutingKey;
    }

    public void setAuthLogoutRoutingKey(String authLogoutRoutingKey) {
        this.authLogoutRoutingKey = authLogoutRoutingKey;
    }
}
