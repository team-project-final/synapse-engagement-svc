package io.synapse.gamification.api;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice(assignableTypes = GamificationController.class)
class GamificationExceptionHandler {

    @ExceptionHandler(MissingRequestHeaderException.class)
    ProblemDetail handleMissingUser(HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "X-User-Id header is required until platform JWT integration is available.");
        decorate(detail, "ENGM-201", request);
        return detail;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail handleValidation(HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid gamification request.");
        decorate(detail, "ENGM-202", request);
        return detail;
    }

    private void decorate(ProblemDetail detail, String code, HttpServletRequest request) {
        detail.setType(URI.create("https://api.synapse.app/errors/" + code));
        detail.setTitle(detail.getStatus() + " " + code);
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("code", code);
    }
}
