package com.deoksan.share.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

/**
 * 조립형 에러 코드.
 *
 * <p>도메인/오퍼레이션/디테일을 조합하여 코드와 메시지를 생성한다.
 * 메시지는 MessageSource를 통해 다국어로 해결된다.
 *
 * <h3>사용 예</h3>
 * <pre>
 * // 서비스에서 조합
 * ErrorCode code = AssembledErrorCode.of(
 *     ErrorDomain.AUT,
 *     ErrorOperation.EML,
 *     ErrorDetail.FMT,
 *     HttpStatus.BAD_REQUEST);
 *
 * // 결과:
 * //   code    → AUT-EML-FMT
 * //   message → (Locale에 따라) "계정 인증, 이메일 인증, 형식 오류" 또는
 * //                                  "Account Auth, Email Verify, Format Invalid"
 * </pre>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AssembledErrorCode implements ErrorCode {

    private final ErrorDomain domain;
    private final ErrorOperation operation;
    private final ErrorDetail detail;
    private final HttpStatus status;

    private static MessageSource messageSource;

    public static void setMessageSource(MessageSource source) {
        AssembledErrorCode.messageSource = source;
    }

    /**
     * 도메인/오퍼레이션/디테일을 조합하여 에러 코드를 생성한다.
     */
    public static AssembledErrorCode compose(ErrorDomain domain, ErrorOperation operation,
                                            ErrorDetail detail, HttpStatus status) {
        return new AssembledErrorCode(domain, operation, detail, status);
    }

    @Override
    public String getCode() {
        return domain.getCode() + "-" + operation.getCode() + "-" + detail.getCode();
    }

    /**
     * 조합된 메시지 — 클라이언트 Locale에 따라 다국어로 해결된다.
     *
     * <p>해결 순서:
     * <ol>
     *   <li>MessageSource에서 i18n 키로 번역 시도</li>
     *   <li>키가 없으면 도메인.label → operation.label → detail.label 조합</li>
     *   <li>영어도 없으면 한글 기본값으로 폴백</li>
     * </ol>
     */
    @Override
    public String getMessage() {
        if (messageSource == null) {
            return fallbackMessage();
        }

        String domainMsg = resolve(domain.getI18nKey(), domain.getLabel());
        String opMsg = resolve(operation.getI18nKey(), operation.getLabel());
        String detailMsg = resolve(detail.getI18nKey(), detail.getDefaultValueKo());

        return domainMsg + ", " + opMsg + ", " + detailMsg;
    }

    /**
     * MessageSource에서 메시지를 해결한다.
     * 키가 없으면 defaultValue를 반환한다.
     */
    private String resolve(String key, String defaultValue) {
        try {
            return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * MessageSource가 설정되지 않았을 때 사용하는 폴백 메시지.
     */
    private String fallbackMessage() {
        return domain.getLabel() + ", " + operation.getLabel() + ", " + detail.getDefaultValueKo();
    }
}
