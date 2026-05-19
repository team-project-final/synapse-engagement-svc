package io.synapse.community.group.api;

import io.synapse.community.group.exception.GroupAccessDeniedException;
import io.synapse.community.group.exception.GroupException;
import io.synapse.community.group.exception.GroupLimitExceededException;
import io.synapse.community.group.exception.GroupNotFoundException;
import io.synapse.community.group.exception.InvalidGroupCursorException;
import io.synapse.community.member.api.MemberController;
import io.synapse.community.member.exception.MemberAlreadyExistsException;
import io.synapse.community.member.exception.MemberNotFoundException;
import io.synapse.community.member.exception.MemberPermissionDeniedException;
import io.synapse.community.member.exception.MemberRejoinBlockedException;
import io.synapse.community.member.exception.OwnerCannotLeaveException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice(assignableTypes = {GroupController.class, MemberController.class})
// 그룹/멤버 API에서 발생한 예외를 RFC 7807 ProblemDetail 응답으로 통일합니다.
class GroupExceptionHandler {

    @ExceptionHandler(GroupNotFoundException.class)
    ProblemDetail handleNotFound(GroupNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    ProblemDetail handleMemberNotFound(MemberNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler({GroupAccessDeniedException.class, MemberPermissionDeniedException.class})
    ProblemDetail handleAccessDenied(GroupException exception, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, exception, request);
    }

    @ExceptionHandler({
            GroupLimitExceededException.class,
            InvalidGroupCursorException.class,
            MemberAlreadyExistsException.class,
            MemberRejoinBlockedException.class,
            OwnerCannotLeaveException.class})
    ProblemDetail handleBadRequest(GroupException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ProblemDetail handleMissingUser(HttpServletRequest request) {
        // JWT 연동 전 임시 인증 헤더가 없으면 미인증으로 처리합니다.
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "X-User-Id header is required until platform JWT integration is available.");
        decorate(detail, "ENGM-005", request);
        return detail;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class})
    ProblemDetail handleValidation(Exception exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid group request.");
        decorate(detail, "ENGM-006", request);
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        decorate(detail, "ENGM-007", request);
        return detail;
    }

    private ProblemDetail problem(HttpStatus status, GroupException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        decorate(detail, exception.code(), request);
        return detail;
    }

    private void decorate(ProblemDetail detail, String code, HttpServletRequest request) {
        // 프론트엔드와 운영 로그에서 같은 에러를 추적할 수 있게 공통 속성을 붙입니다.
        detail.setType(URI.create("https://api.synapse.app/errors/" + code));
        detail.setTitle(detail.getStatus() + " " + code);
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("code", code);
    }
}
