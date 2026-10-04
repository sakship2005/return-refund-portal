package com.portal.rrp.controller;

import com.portal.rrp.model.ReturnRequest;
import com.portal.rrp.service.ReturnRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/requests")
public class ReturnRequestController {

    @Autowired
    private ReturnRequestService service;

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("requests", service.search(q));
        model.addAttribute("q", q);
        return "requests-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("request", new ReturnRequest());
        return "request-form";
    }

    @PostMapping
    public String create(@ModelAttribute ReturnRequest request) {
        service.save(request);
        return "redirect:/requests";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("request", service.findById(id).orElseThrow());
        model.addAttribute("history", service.historyFor(id));
        return "request-detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("request", service.findById(id).orElseThrow());
        return "request-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute ReturnRequest formData) {
        ReturnRequest existing = service.findById(id).orElseThrow();
        existing.setProduct(formData.getProduct());
        existing.setReason(formData.getReason());
        existing.setAmount(formData.getAmount());
        existing.setCustomerName(formData.getCustomerName());
        service.save(existing);
        return "redirect:/requests/" + id;
    }

    @PostMapping("/{id}/advance")
    public String advance(@PathVariable Long id, @RequestParam String actor) {
        service.advanceStatus(id, actor);
        return "redirect:/requests/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam String actor) {
        service.rejectRequest(id, actor);
        return "redirect:/requests/" + id;
    }
}