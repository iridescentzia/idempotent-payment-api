package com.zia.payments.global.controller;

import com.zia.payments.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public ApiResponse<String> health() {
        return ApiResponse.success("OK", "Server is running");
    }
}
