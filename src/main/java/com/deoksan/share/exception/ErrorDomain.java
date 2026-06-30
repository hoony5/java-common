package com.deoksan.share.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드 도메인.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorDomain {
    AUT("AccountAuth", "err.dom.aut", "AUT"),
    TKN("AccountToken", "err.dom.tkn", "TKN"),
    PRF("AccountProfile", "err.dom.prf", "PRF"),
    WDR("AccountWithdraw", "err.dom.wdr", "WDR"),
    DEV("AccountDevice", "err.dom.dev", "DEV"),
    LCK("AccountLock", "err.dom.lck", "LCK"),
    PST("BlogPost", "err.dom.pst", "PST"),
    CLT("OAuthClient", "err.dom.clt", "CLT"),
    KEY("ApiKey", "err.dom.key", "KEY"),
    SY("System", "err.dom.sy", "SY"),
    VL("Validation", "err.dom.vl", "VL"),
    STG("Storage", "err.dom.stg", "STG"),
    CMT("BlogComment", "err.dom.cmt", "CMT");

    private final String label;
    private final String i18nKey;
    private final String code;
}
