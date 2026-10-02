package com.portal.rrp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Entity
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String orderId;

    @NotBlank
    private String product;

    @NotBlank
    private String reason;

    @Positive
    private Double amount;

    @NotBlank
    private String customerName;

    @Enumerated(EnumType.STRING)
    private Status status = Status.REQUESTED;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // getters and setters
    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; this.updatedAt = LocalDateTime.now(); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}