package com.portal.rrp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    // MVP task 15: health check (used later for deployment and rollback)
    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "OK";
    }
}