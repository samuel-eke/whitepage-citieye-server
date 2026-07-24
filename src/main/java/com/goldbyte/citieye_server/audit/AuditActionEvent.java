package com.goldbyte.citieye_server.audit;

import java.time.OffsetDateTime;

/**
 * Public application event published to record administrative actions for governance auditing.
 * The audit module will asynchronously process this event once it is fully implemented in Phase 6.
 */
public class AuditActionEvent {
    private final String actionType;
    private final String actorCode;
    private final String targetId;
    private final String details;
    private final OffsetDateTime timestamp;

    public AuditActionEvent(String actionType, String actorCode, String targetId, String details) {
        this.actionType = actionType;
        this.actorCode = actorCode;
        this.targetId = targetId;
        this.details = details;
        this.timestamp = OffsetDateTime.now();
    }

    public String getActionType() {
        return actionType;
    }

    public String getActorCode() {
        return actorCode;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getDetails() {
        return details;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
