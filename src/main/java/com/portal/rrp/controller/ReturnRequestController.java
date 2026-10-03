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
        return "request-detail";
    }
}