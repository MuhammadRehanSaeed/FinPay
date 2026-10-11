package com.rehancode.finpay.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @PostMapping("hello")
    public String admincon() {
        log.info("POST /api/admin/hello called");
        return "admin";
    }
}
