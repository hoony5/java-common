package com.deoksan.share.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드 디테일 (고정 패턴).
 */
@Getter
@RequiredArgsConstructor
public enum ErrorDetail {
    NONE("001", "없음", "err.dtl.none", "NONE"),
    FMT("002", "형식 오류", "err.dtl.fmt", "FMT"),
    DUP("003", "중복", "err.dtl.dup", "DUP"),
    EXP("004", "만료", "err.dtl.exp", "EXP"),
    PRM("005", "권한 없음", "err.dtl.prm", "PRM");

    private final String code;
    private final String defaultValueKo;
    private final String i18nKey;
    private final String label;
}
