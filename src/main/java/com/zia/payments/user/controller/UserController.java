package com.zia.payments.user.controller;

import com.zia.payments.global.response.ApiResponse;
import com.zia.payments.user.domain.User;
import com.zia.payments.user.dto.request.UserCreateRequest;
import com.zia.payments.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 사용자 생성 : POST /api/users
    @PostMapping
    public ApiResponse<User> createUser(@RequestBody UserCreateRequest request) {
        User user = userService.createUser(request.getName());
        return ApiResponse.success(user);
    }
}
