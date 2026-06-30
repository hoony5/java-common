package com.deoksan.share.audit;

public record AuditCommand(
        Long userId,
        String action,
        String result,
        String ip,
        String detail
) {
    public static AuditCommand success(Long userId, String action, String ip, String detail) {
        return new AuditCommand(userId, action, "SUCCESS", ip, detail);
    }

    public static AuditCommand failure(Long userId, String action, String ip, String detail) {
        return new AuditCommand(userId, action, "FAILURE", ip, detail);
    }
}
