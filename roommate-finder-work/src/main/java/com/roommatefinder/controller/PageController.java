package com.roommatefinder.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }

    @GetMapping("/verify-email")
    public String verifyEmail() { return "verify-email"; }

    @GetMapping("/dashboard")
    public String dashboard() { return "dashboard"; }

    @GetMapping("/profile")
    public String profile() { return "profile"; }

    @GetMapping("/matches")
    public String matches() { return "matches"; }

    @GetMapping("/admin")
    public String admin() { return "admin"; }

    @GetMapping("/forgot-password")
    public String forgotPassword() { return "forgot-password"; }

    @GetMapping("/reset-password")
    public String resetPassword() { return "reset-password"; }

    @GetMapping("/messages")
    public String messages() { return "messages"; }

    @GetMapping("/chat")
    public String chat() { return "chat"; }
}
