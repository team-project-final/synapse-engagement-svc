package com.synapse.engagement.community.exception;

import com.synapse.engagement.community.controller.SharedContentController;
import com.synapse.engagement.community.exception.SharedContentAccessDeniedException;
import com.synapse.engagement.community.exception.SharedContentException;
import com.synapse.engagement.community.exception.SharedContentNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice(assignableTypes = SharedContentController.class)
class SharedContentExceptionHandler {

    /*
     * Step 5 공유 API 전용 예외 변환기입니다.
     * Service/Entity는 비즈니스 예외만 던지고, 여기서 HTTP 상태 코드와 ProblemDetail 모양으로 바꿉니다.
     */
    @ExceptionHandler(SharedContentNotFoundException.class)
    ProblemDetail handleNotFound(SharedContentNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler(SharedContentAccessDeniedException.class)
    ProblemDetail handleAccessDenied(SharedContentAccessDeniedException exception, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, exception, request);
    }

    @ExceptionHandler(SharedContentException.class)
    ProblemDetail handleSharedContentException(SharedContentException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ProblemDetail handleMissingUser(HttpServletRequest request) {
        /*
         * 공유 등록/fork/delete는 사용자 소유권이 필요한 API입니다.
         * platform-svc JWT 연동 전까지는 X-User-Id 임시 헤더가 없으면 미인증으로 봅니다.
         */
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "X-User-Id header is required until platform JWT integration is available.");
        decorate(detail, "ENGM-304", request);
        return detail;
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class})
    ProblemDetail handleValidation(Exception exception, HttpServletRequest request) {
        // Request body 검증 실패나 size 범위 오류처럼 클라이언트 입력이 잘못된 경우입니다.
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid shared content request.");
        decorate(detail, "ENGM-305", request);
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        decorate(detail, "ENGM-306", request);
        return detail;
    }

    private ProblemDetail problem(HttpStatus status, SharedContentException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        decorate(detail, exception.code(), request);
        return detail;
    }

    private void decorate(ProblemDetail detail, String code, HttpServletRequest request) {
        // 모든 공유 API 에러가 같은 형태를 갖도록 공통 추적 정보를 채웁니다.
        detail.setType(URI.create("https://api.synapse.app/errors/" + code));
        detail.setTitle(detail.getStatus() + " " + code);
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("code", code);
    }
}

