package com.portal.rrp.controller;

import com.portal.rrp.service.ReturnRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private ReturnRequestService service;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("counts", service.countsByStatus());
        model.addAttribute("totalRefund", service.totalRefundAmount());
        return "dashboard";
    }
}