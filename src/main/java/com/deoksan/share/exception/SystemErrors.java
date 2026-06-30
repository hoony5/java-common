package com.deoksan.share.exception;

import org.springframework.http.HttpStatus;

import static com.deoksan.share.exception.AssembledErrorCode.compose;
import static com.deoksan.share.exception.ErrorDetail.NONE;
import static com.deoksan.share.exception.ErrorDomain.*;
import static com.deoksan.share.exception.ErrorOperation.*;

public final class SystemErrors {

    private SystemErrors() {}

    public static final ErrorCode RATE_LIMIT_EXCEEDED =
            compose(SY, RTL, NONE, HttpStatus.TOO_MANY_REQUESTS);

    public static final ErrorCode VALIDATION_FAILED =
            compose(VL, VD, NONE, HttpStatus.BAD_REQUEST);

    public static final ErrorCode INVALID_ARGUMENT =
            compose(VL, ARG, NONE, HttpStatus.BAD_REQUEST);

    public static final ErrorCode ACCESS_DENIED =
            compose(SY, ACC, NONE, HttpStatus.FORBIDDEN);

    public static final ErrorCode METHOD_NOT_ALLOWED =
            compose(SY, ARG, NONE, HttpStatus.METHOD_NOT_ALLOWED);

    public static final ErrorCode UNSUPPORTED_MEDIA_TYPE =
            compose(SY, ARG, NONE, HttpStatus.UNSUPPORTED_MEDIA_TYPE);

    public static final ErrorCode NOT_FOUND =
            compose(SY, LKP, NONE, HttpStatus.NOT_FOUND);

    public static final ErrorCode SERVER_ERROR =
            compose(SY, ERR, NONE, HttpStatus.INTERNAL_SERVER_ERROR);
}
