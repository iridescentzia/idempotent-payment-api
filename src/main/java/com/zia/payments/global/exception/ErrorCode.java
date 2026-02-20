package com.zia.payments.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),

    // 지갑/포인트 관련
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND", "포인트 지갑을 찾을 수 없습니다"),
    INSUFFICIENT_BALANCE(HttpStatus.CONFLICT, "INSUFFICIENT_BALANCE", "포인트 잔액이 부족합니다"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "포인트 금액은 0보다 커야 합니다"),

    // 멱등성 관련
    IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key가 필요합니다"),
    IDEMPOTENCY_IN_PROGRESS(HttpStatus.CONFLICT, "IDEMPOTENCY_IN_PROGRESS", "이미 처리 중인 요청입니다"),
    IDEMPOTENCY_FAILED(HttpStatus.CONFLICT, "IDEMPOTENCY_FAILED", "이전 요청이 실패했습니다. 새로운 Idempotency-Key로 재요청하세요."),

    // 공통
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다"),

    // 쿠폰 관련
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON_NOT_FOUND", "쿠폰을 찾을 수 없습니다"),
    COUPON_EXPIRED(HttpStatus.CONFLICT, "COUPON_EXPIRED", "쿠폰이 만료되었습니다"),
    COUPON_SOLD_OUT(HttpStatus.CONFLICT, "COUPON_SOLD_OUT", "쿠폰이 품절되었습니다"),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "COUPON_ALREADY_ISSUED", "이미 발급받은 쿠폰입니다"),
    COUPON_ALREADY_USED(HttpStatus.CONFLICT, "COUPON_ALREADY_USED", "이미 사용된 쿠폰입니다"),
    COUPON_NOT_OWNED(HttpStatus.FORBIDDEN, "COUPON_NOT_OWNED", "쿠폰 소유자가 아닙니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
