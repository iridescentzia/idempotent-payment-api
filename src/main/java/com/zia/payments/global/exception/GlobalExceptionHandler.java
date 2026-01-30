package com.zia.payments.global.exception;

import com.zia.payments.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiResponse<Void>> buildError(ErrorCode errorCode, String message) {
        ApiResponse<Void> response = ApiResponse.error(errorCode.getCode(), message);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    private ResponseEntity<ApiResponse<Void>> buildError(ErrorCode errorCode) {
        return buildError(errorCode, errorCode.getMessage());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e) {
        log.warn("ApiException: {}", e.getMessage());
        return buildError(e.getErrorCode(), e.getMessage());
    }

    // JSON body 누락/파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        return buildError(ErrorCode.BAD_REQUEST, "요청 본문(JSON)이 없거나 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException: {}", e.getMessage());

        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse(ErrorCode.BAD_REQUEST.getMessage());

        return buildError(ErrorCode.BAD_REQUEST, message);
    }

    // 타입 변환 실패
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("MethodArgumentTypeMismatchException: {}", e.getMessage());
        return buildError(ErrorCode.BAD_REQUEST, "요청 파라미터 타입이 올바르지 않습니다.");
    }

    // 정적 리소스 없음 (favicon 등) → 404
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.debug("NoResourceFoundException: {}", e.getMessage());
        return ResponseEntity.status(404)
                .body(ApiResponse.error("NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleIllegalException(RuntimeException e) {
        log.warn("IllegalException: {}", e.getMessage());
        return buildError(ErrorCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unexpected exception", e);
        return buildError(ErrorCode.INTERNAL_ERROR);
    }
}
