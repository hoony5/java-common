package com.deoksan.share.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드 계약.
 */
public interface ErrorCode {
    String getCode();
    String getMessage();
    HttpStatus getStatus();
}
