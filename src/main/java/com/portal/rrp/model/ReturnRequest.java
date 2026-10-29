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

    // username of the logged-in user who submitted the request
    private String createdBy;

    @Lob
    @Column(name = "product_image", length = 10485760)
    private byte[] productImage;

    private String productImageContentType;
    private String productImageFileName;

    @Lob
    @Column(name = "bill_image", length = 10485760)
    private byte[] billImage;

    private String billImageContentType;
    private String billImageFileName;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; this.updatedAt = LocalDateTime.now(); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public byte[] getProductImage() { return productImage; }
    public void setProductImage(byte[] productImage) { this.productImage = productImage; }
    public String getProductImageContentType() { return productImageContentType; }
    public void setProductImageContentType(String productImageContentType) { this.productImageContentType = productImageContentType; }
    public String getProductImageFileName() { return productImageFileName; }
    public void setProductImageFileName(String productImageFileName) { this.productImageFileName = productImageFileName; }

    public byte[] getBillImage() { return billImage; }
    public void setBillImage(byte[] billImage) { this.billImage = billImage; }
    public String getBillImageContentType() { return billImageContentType; }
    public void setBillImageContentType(String billImageContentType) { this.billImageContentType = billImageContentType; }
    public String getBillImageFileName() { return billImageFileName; }
    public void setBillImageFileName(String billImageFileName) { this.billImageFileName = billImageFileName; }

    public boolean hasProductImage() {
        return productImage != null && productImage.length > 0;
    }

    public boolean hasBillImage() {
        return billImage != null && billImage.length > 0;
    }
}
