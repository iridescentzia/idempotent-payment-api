package com.zia.payments.user.controller;

import com.zia.payments.global.response.ApiResponse;
import com.zia.payments.user.domain.User;
import com.zia.payments.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 사용자 생성 : POST /api/users
    @PostMapping
    public ApiResponse<User> createUser(@RequestParam String name) {
        User user = userService.createUser(name);
        return ApiResponse.success(user);
    }
}
