package com.zia.payments.global.exception;

import com.zia.payments.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e) {
        log.warn("ApiException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.error(e.getMessage());
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * 정적 리소스 없음 (favicon.ico 등) → 404로 처리
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.debug("NoResourceFoundException: {}", e.getMessage());
        ApiResponse<Void> response = ApiResponse.error("요청한 리소스를 찾을 수 없습니다.");
        return ResponseEntity
                .status(404)
                .body(response);
    }

    /**
     * 엔티티에서 던지는 IllegalArgumentException / IllegalStateException
     * (PointWallet increase/decrease 같은 것들)
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleIllegalException(RuntimeException e) {
        log.warn("IllegalException: {}", e.getMessage());
        ApiResponse<Void> response = ApiResponse.error(e.getMessage());
        return ResponseEntity
                .status(400)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unexpected exception", e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INTERNAL_ERROR.getMessage()
        );
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(response);
    }
}
