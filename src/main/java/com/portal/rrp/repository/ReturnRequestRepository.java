package com.portal.rrp.repository;

import com.portal.rrp.model.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findByOrderIdContainingIgnoreCaseOrCustomerNameContainingIgnoreCase(
            String orderId, String customerName);
}