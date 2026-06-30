package com.deoksan.share.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드 오퍼레이션.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorOperation {
    SES("Session", "err.op.ses", "SES"),
    EML("EmailVerify", "err.op.eml", "EML"),
    ACC("Access", "err.op.acc", "ACC"),
    REF("Refresh", "err.op.ref", "REF"),
    LKP("Lookup", "err.op.lkp", "LKP"),
    UPD("Update", "err.op.upd", "UPD"),
    REQ("Request", "err.op.req", "REQ"),
    CAN("Cancel", "err.op.can", "CAN"),
    PKE("Passkey", "err.op.pke", "PKE"),
    ADM("Admin", "err.op.adm", "ADM"),
    CRT("Create", "err.op.crt", "CRT"),
    DLT("Delete", "err.op.dlt", "DLT"),
    RTL("RateLimit", "err.op.rtl", "RTL"),
    VD("Validate", "err.op.vd", "VD"),
    ARG("Argument", "err.op.arg", "ARG"),
    ERR("ServerError", "err.op.err", "ERR"),
    ROT("Rotate", "err.op.rot", "ROT"),
    SCO("Scope", "err.op.sco", "SCO");

    private final String label;
    private final String i18nKey;
    private final String code;
}
