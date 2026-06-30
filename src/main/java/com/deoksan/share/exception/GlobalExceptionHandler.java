package com.deoksan.share.exception;

import com.deoksan.share.dto.ErrorDetailResponse;
import com.deoksan.share.dto.ApiResponse;
import com.deoksan.share.exception.SystemErrors;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.List;

/**
 * 전역 예외 처리 핸들러.
 *
 * <p>모든 컨트롤러에서 발생하는 예외를 가로채
 * 일관된 {@link ApiResponse} 형식으로 응답한다.
 *
 * <h3>처리 순서</h3>
 * <ol>
 *   <li>NotFoundException — 리소스 없음 (404)</li>
 *   <li>BusinessException — 비즈니스 규칙 위반 (정의된 상태 코드)</li>
 *   <li>MethodArgumentNotValidException — 벨리데이션 실패 (400)</li>
 *   <li>Exception — 처리되지 않은 오류 (500)</li>
 * </ol>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // ─── 리소스 없음 ───────────────────────────────────────────────────────

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        ErrorCode code = ex.getErrorCode();
        log.warn("[{}] 리소스 없음", code.getCode());
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.failure(code.getCode(), code.getMessage()));
    }

    // ─── 비즈니스 규칙 위반 ─────────────────────────────────────────────────

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode code = ex.getErrorCode();
        log.warn("[{}] 비즈니스 규칙 위반", code.getCode());
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(code.getStatus());
        // RFC 7235: 401 응답에 WWW-Authenticate 헤더 필수
        if (code.getStatus() == HttpStatus.UNAUTHORIZED) {
            builder.header(HttpHeaders.WWW_AUTHENTICATE,
                    "Bearer realm=\"personalBlog\", error=\"" + code.getCode() + "\"");
        }
        return builder.body(ApiResponse.failure(code.getCode(), code.getMessage()));
    }

    // ─── 벨리데이션 실패 ────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetailResponse>> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<ErrorDetailResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> ErrorDetailResponse.FieldError.of(fe.getField(), fe.getDefaultMessage()))
                .toList();

        String summary = messageSource.getMessage(
                "validation.summary", null, LocaleContextHolder.getLocale());

        log.warn("[{}] 벨리데이션 실패: {}건", SystemErrors.VALIDATION_FAILED.getCode(), fieldErrors.size());

        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(SystemErrors.VALIDATION_FAILED.getCode(), summary,
                        ErrorDetailResponse.of(fieldErrors)));
    }

    // ─── Rate Limit 초과 ────────────────────────────────────────────────────

    /**
     * [신규] API 호출 빈도 초과 — HTTP 429 Too Many Requests.
     *
     * [이전 문제] RateLimitAspect가 IllegalArgumentException을 던져서
     * 이 핸들러의 handleIllegalArgument로 잡혀 400이 반환됐음.
     * 전용 예외(RateLimitExceededException)와 전용 핸들러로 분리하여
     * RFC 6585 표준인 429를 반환하도록 수정.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimit(RateLimitExceededException ex) {
        log.warn("[RATE-LIMIT] 호출 제한 초과: key={}, count={}/{}",
                ex.getLimitKey(), ex.getCurrentCount(), ex.getMaxCount());
        // RFC 6585: 429 응답에 Retry-After 헤더를 포함해야 함
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()));
        return ResponseEntity.status(SystemErrors.RATE_LIMIT_EXCEEDED.getStatus())
                .headers(headers)
                .body(ApiResponse.failure(SystemErrors.RATE_LIMIT_EXCEEDED.getCode(), ex.getMessage()));
    }

    // ─── 접근 거부 ─────────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("[{}] 접근 거부: {}", SystemErrors.ACCESS_DENIED.getCode(), ex.getMessage());
        return ResponseEntity.status(SystemErrors.ACCESS_DENIED.getStatus())
                .body(ApiResponse.failure(SystemErrors.ACCESS_DENIED.getCode(), "접근 권한이 없습니다"));
    }

    // ─── HTTP 메서드 불일치 ─────────────────────────────────────────────────

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        log.warn("[{}] 허용되지 않은 HTTP 메서드: {}", SystemErrors.METHOD_NOT_ALLOWED.getCode(), ex.getMethod());
        return ResponseEntity.status(SystemErrors.METHOD_NOT_ALLOWED.getStatus())
                .body(ApiResponse.failure(SystemErrors.METHOD_NOT_ALLOWED.getCode(), "허용되지 않은 HTTP 메서드입니다"));
    }

    // ─── 요청 본문 파싱 실패 ────────────────────────────────────────────────

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("[{}] 요청 본문 파싱 실패", SystemErrors.INVALID_ARGUMENT.getCode());
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(SystemErrors.INVALID_ARGUMENT.getCode(), "요청 본문 형식이 올바르지 않습니다"));
    }

    // ─── 미디어 타입 불일치 ─────────────────────────────────────────────────

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("[{}] 지원하지 않는 미디어 타입: {}", SystemErrors.UNSUPPORTED_MEDIA_TYPE.getCode(), ex.getContentType());
        return ResponseEntity.status(SystemErrors.UNSUPPORTED_MEDIA_TYPE.getStatus())
                .body(ApiResponse.failure(SystemErrors.UNSUPPORTED_MEDIA_TYPE.getCode(), "지원하지 않는 Content-Type입니다"));
    }

    // ─── 필수 파라미터 누락 ─────────────────────────────────────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("[{}] 필수 파라미터 누락: {}", SystemErrors.VALIDATION_FAILED.getCode(), ex.getParameterName());
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(SystemErrors.VALIDATION_FAILED.getCode(),
                        "필수 파라미터가 누락되었습니다: " + ex.getParameterName()));
    }

    // ─── @Validated 서비스 레벨 제약 위반 ─────────────────────────────────

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("[{}] 제약 조건 위반: {}건", SystemErrors.VALIDATION_FAILED.getCode(),
                ex.getConstraintViolations().size());
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(SystemErrors.VALIDATION_FAILED.getCode(), "입력값이 제약 조건을 위반했습니다"));
    }

    // ─── 핸들러 없음 (404) ──────────────────────────────────────────────────

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.warn("[{}] 핸들러 없음: {} {}", SystemErrors.NOT_FOUND.getCode(),
                ex.getHttpMethod(), ex.getRequestURL());
        return ResponseEntity.status(SystemErrors.NOT_FOUND.getStatus())
                .body(ApiResponse.failure(SystemErrors.NOT_FOUND.getCode(), "요청한 리소스를 찾을 수 없습니다"));
    }

    // ─── 잘못된 인자 ───────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("[{}] 잘못된 인자: {}", SystemErrors.INVALID_ARGUMENT.getCode(), ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(SystemErrors.INVALID_ARGUMENT.getCode(), "잘못된 요청입니다"));
    }

    // ─── 처리되지 않은 예외 ─────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("[{}] 예상치 못한 오류 발생", SystemErrors.SERVER_ERROR.getCode(), ex);
        return ResponseEntity.status(SystemErrors.SERVER_ERROR.getStatus())
                .body(ApiResponse.failure(SystemErrors.SERVER_ERROR.getCode(), "서버 내부 오류가 발생했습니다"));
    }
}
