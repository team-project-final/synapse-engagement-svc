package com.synapse.engagement.gamification.exception;

import com.synapse.engagement.gamification.controller.GamificationController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice(assignableTypes = GamificationController.class)
class GamificationExceptionHandler {

    /*
     * GamificationController에서 발생하는 예외만 REST 응답으로 바꾸는 클래스입니다.
     * 서비스 로직은 HTTP 상태 코드를 몰라도 되고, Controller 바깥에서 API 응답 모양을 통일할 수 있습니다.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    ProblemDetail handleMissingUser(HttpServletRequest request) {
        /*
         * Step 4에서는 로그인 사용자를 임시 X-User-Id 헤더로 받습니다.
         * 이 헤더가 없으면 "누구의 XP를 조회할지" 알 수 없으므로 401로 응답합니다.
         */
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "X-User-Id header is required until platform JWT integration is available.");
        decorate(detail, "ENGM-201", request);
        return detail;
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    ProblemDetail handleValidation(HttpServletRequest request) {
        /*
         * 예: /xp/history?size=0 또는 size=999처럼 @Min/@Max 범위를 벗어난 요청입니다.
         * 요청 값 자체가 잘못된 것이므로 400 Bad Request로 돌려줍니다.
         */
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid gamification request.");
        decorate(detail, "ENGM-202", request);
        return detail;
    }

    private void decorate(ProblemDetail detail, String code, HttpServletRequest request) {
        /*
         * ProblemDetail은 RFC 9457 형식의 표준 에러 응답입니다.
         * code/type/instance를 채워두면 프론트와 로그에서 어떤 API가 왜 실패했는지 추적하기 쉽습니다.
         */
        detail.setType(URI.create("https://api.synapse.app/errors/" + code));
        detail.setTitle(detail.getStatus() + " " + code);
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("code", code);
    }
}

