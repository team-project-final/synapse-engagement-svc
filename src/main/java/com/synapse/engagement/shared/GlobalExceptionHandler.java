package com.synapse.engagement.shared;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        // 도메인 서비스는 BusinessException 계층만 던지고, HTTP 상태/응답 형식 변환은 여기서 통일한다.
        return ResponseEntity.status(ex.status()).body(ApiError.of(ex.code(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleInvalidBody(MethodArgumentNotValidException ex) {
        // @Valid request body 실패는 첫 번째 필드 오류를 사용해 클라이언트가 바로 고칠 수 있게 한다.
        var message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Request validation failed");
        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_FAILED", message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleInvalidParam(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_FAILED", ex.getMessage()));
    }
}
