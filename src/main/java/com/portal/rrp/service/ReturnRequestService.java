package com.portal.rrp.service;

import com.portal.rrp.model.ReturnRequest;
import com.portal.rrp.model.Status;
import com.portal.rrp.model.StatusHistory;
import com.portal.rrp.repository.ReturnRequestRepository;
import com.portal.rrp.repository.StatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReturnRequestService {

    @Autowired
    private ReturnRequestRepository repository;

    @Autowired
    private StatusHistoryRepository historyRepository;

    public ReturnRequest save(ReturnRequest request) {
        return repository.save(request);
    }

    public List<ReturnRequest> findAll() {
        return repository.findAll();
    }

    public Optional<ReturnRequest> findById(Long id) {
        return repository.findById(id);
    }

    public List<ReturnRequest> search(String query) {
        if (query == null || query.isBlank()) {
            return repository.findAll();
        }
        return repository.findByOrderIdContainingIgnoreCaseOrCustomerNameContainingIgnoreCase(query, query);
    }

    // Allowed transitions: current status -> next status
    private static final Map<Status, Status> NEXT = Map.of(
        Status.REQUESTED, Status.UNDER_REVIEW,
        Status.UNDER_REVIEW, Status.APPROVED,      // "reject" handled separately
        Status.APPROVED, Status.ITEM_RECEIVED,
        Status.ITEM_RECEIVED, Status.REFUND_PROCESSED,
        Status.REFUND_PROCESSED, Status.CLOSED
    );

    public ReturnRequest advanceStatus(Long id, String changedBy) {
        ReturnRequest r = repository.findById(id).orElseThrow();
        Status old = r.getStatus();
        Status next = NEXT.get(old);
        if (next == null) {
            throw new IllegalStateException("No further transition allowed from " + old);
        }
        r.setStatus(next);
        repository.save(r);
        logHistory(id, old, next, changedBy);
        return r;
    }

    public ReturnRequest rejectRequest(Long id, String changedBy) {
        ReturnRequest r = repository.findById(id).orElseThrow();
        Status old = r.getStatus();
        if (old != Status.UNDER_REVIEW) {
            throw new IllegalStateException("Can only reject from UNDER_REVIEW");
        }
        r.setStatus(Status.REJECTED);
        repository.save(r);
        logHistory(id, old, Status.REJECTED, changedBy);
        return r;
    }

    private void logHistory(Long requestId, Status old, Status next, String changedBy) {
        StatusHistory h = new StatusHistory();
        h.setRequestId(requestId);
        h.setOldStatus(old);
        h.setNewStatus(next);
        h.setChangedBy(changedBy);
        historyRepository.save(h);
    }

    public List<StatusHistory> historyFor(Long requestId) {
        return historyRepository.findByRequestIdOrderByChangedAtAsc(requestId);
    }

    // Dashboard summary
    public Map<Status, Long> countsByStatus() {
        return repository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(ReturnRequest::getStatus, java.util.stream.Collectors.counting()));
    }

    public double totalRefundAmount() {
        return repository.findAll().stream()
                .filter(r -> r.getStatus() == Status.REFUND_PROCESSED || r.getStatus() == Status.CLOSED)
                .mapToDouble(r -> r.getAmount() == null ? 0 : r.getAmount())
                .sum();
    }
}