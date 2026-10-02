package com.portal.rrp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long requestId;

    @Enumerated(EnumType.STRING)
    private Status oldStatus;

    @Enumerated(EnumType.STRING)
    private Status newStatus;

    private String changedBy;
    private LocalDateTime changedAt = LocalDateTime.now();

    // getters and setters
    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public Status getOldStatus() { return oldStatus; }
    public void setOldStatus(Status oldStatus) { this.oldStatus = oldStatus; }
    public Status getNewStatus() { return newStatus; }
    public void setNewStatus(Status newStatus) { this.newStatus = newStatus; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
}