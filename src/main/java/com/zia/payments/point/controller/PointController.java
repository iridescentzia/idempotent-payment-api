package com.zia.payments.point.controller;

import com.zia.payments.global.exception.ApiException;
import com.zia.payments.global.exception.ErrorCode;
import com.zia.payments.global.response.ApiResponse;
import com.zia.payments.point.dto.request.ChargeRequest;
import com.zia.payments.point.dto.request.RedeemRequest;
import com.zia.payments.point.dto.response.BalanceResponse;
import com.zia.payments.point.dto.response.ChargeResponse;
import com.zia.payments.point.dto.response.RedeemResponse;
import com.zia.payments.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    // 포인트 충전 : POST /api/users/{userId}/points/charge
    @PostMapping("/charge")
    public ApiResponse<ChargeResponse> chargePoints(
            @PathVariable Long userId,
            @RequestBody ChargeRequest request
    ) {
        ChargeResponse response = pointService.charge(
                userId,
                request.getAmount(),
                request.getMemo()
        );
        return ApiResponse.success(response);
    }

    // 포인트 잔액 조회 : GET /api/users/{userId}/points/balance
    @GetMapping("/balance")
    public ApiResponse<BalanceResponse> getBalance(@PathVariable Long userId) {
        Long balance = pointService.getBalance(userId);
        return ApiResponse.success(
                BalanceResponse.builder()
                        .userId(userId)
                        .balance(balance)
                        .build()
        );
    }

    // 포인트 차감 (결제) : POST /api/users/{userId}/redeem
    // Header : Idempotency-Key
    @PostMapping("/redeem")
    public ApiResponse<RedeemResponse> redeemPoints(
            @PathVariable long userId,
            @RequestBody RedeemRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String requestId
    ) {

        RedeemResponse response = pointService.redeem(
                userId,
                request.getAmount(),
                request.getMemo(),
                requestId
        );
        return ApiResponse.success(response);
    }
}
