package com.zia.payments.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),

    // 지갑/포인트 관련
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "포인트 지갑을 찾을 수 없습니다"),
    INSUFFICIENT_BALANCE(HttpStatus.CONFLICT, "포인트 잔액이 부족합니다"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "포인트 금액은 0보다 커야 합니다"),

    // 공통
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다");

    private final HttpStatus status;
    private final String message;
}
