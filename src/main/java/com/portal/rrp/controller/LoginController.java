package com.portal.rrp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // The form itself is posted to /login and handled by Spring Security.
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
