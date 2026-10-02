package com.portal.rrp.repository;

import com.portal.rrp.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByRequestIdOrderByChangedAtAsc(Long requestId);
}