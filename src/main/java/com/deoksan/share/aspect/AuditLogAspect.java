package com.deoksan.share.aspect;

import com.deoksan.share.annotation.AuditLog;
import com.deoksan.share.audit.AuditCommand;
import com.deoksan.share.audit.AuditService;
import com.deoksan.share.config.logging.MdcKey;
import com.deoksan.share.util.IpUtils;
import com.deoksan.share.util.LogSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditService auditService;

    @Around("@annotation(auditLog)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        Long userId = parseUserId(LogSanitizer.clean(resolveUserId()));
        String action = auditLog.action();
        String description = auditLog.description();
        String ip = resolveIp();

        MDC.put(MdcKey.USER_ID, userId == null ? "" : userId.toString());
        MDC.put(MdcKey.AUDIT_ACTION, action);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            log.info("[AUDIT] user={}, action={}, description={}, result=SUCCESS, duration={}ms",
                    userId, action, description, duration);
            auditService.record(AuditCommand.success(
                    userId, action, ip, description.isEmpty() ? null : description));

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.warn("[AUDIT] user={}, action={}, description={}, result=FAILURE, error={}, duration={}ms",
                    userId, action, description, ex.getClass().getSimpleName(), duration);
            auditService.record(AuditCommand.failure(
                    userId, action, ip, ex.getClass().getSimpleName() + ": " + ex.getMessage()));
            throw ex;
        } finally {
            MDC.remove(MdcKey.USER_ID);
            MDC.remove(MdcKey.AUDIT_ACTION);
        }
    }

    private String resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    private Long parseUserId(String userId) {
        if (userId == null) return null;
        try { return Long.valueOf(userId); } catch (NumberFormatException e) { return null; }
    }

    private String resolveIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            return IpUtils.resolveClientIp(request);
        } catch (Exception e) {
            return null;
        }
    }
}
