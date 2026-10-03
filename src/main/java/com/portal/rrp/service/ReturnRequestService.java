package com.portal.rrp.service;

import com.portal.rrp.model.ReturnRequest;
import com.portal.rrp.repository.ReturnRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReturnRequestService {

    @Autowired
    private ReturnRequestRepository repository;

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
}