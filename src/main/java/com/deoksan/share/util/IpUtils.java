package com.deoksan.share.util;

import jakarta.servlet.http.HttpServletRequest;

import com.deoksan.share.web.HttpHeader;
import java.util.regex.Pattern;

/**
 * 클라이언트 IP 추출 유틸리티.
 *
 * 우선순위:
 *  1. CF-Connecting-IP (Cloudflare 배포 시) — 엣지에서 설정되므로 클라이언트 위조 불가
 *  2. X-Forwarded-For 첫 번째 값 (IP 형식 검증 후 신뢰)
 *  3. remoteAddr (직접 연결 또는 위 헤더가 없을 때)
 */
public final class IpUtils {

    private static final Pattern IPV4 = Pattern.compile(
            "^(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)" +
            "(\\.(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)){3}$");
    private static final Pattern IPV6 = Pattern.compile(
            "^[0-9a-fA-F:]{2,39}$");

    private IpUtils() {}

    /**
     * 요청에서 실제 클라이언트 IP를 추출한다.
     *
     * Cloudflare 환경에서는 CF-Connecting-IP를 최우선으로 사용한다.
     * 해당 헤더가 없으면 X-Forwarded-For 첫 번째 값을 형식 검증 후 사용하고,
     * 그것도 없으면 remoteAddr를 반환한다.
     */
    public static String resolveClientIp(HttpServletRequest request) {
        // 1순위: Cloudflare 실제 클라이언트 IP
        String cfIp = request.getHeader(HttpHeader.CF_CONNECTING_IP);
        if (cfIp != null && !cfIp.isBlank() && isValidIp(cfIp.trim())) {
            return cfIp.trim();
        }

        // 2순위: X-Forwarded-For (Nginx 등 다른 프록시)
        String forwarded = request.getHeader(HttpHeader.X_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            String candidate = forwarded.split(",")[0].trim();
            if (isValidIp(candidate)) {
                return candidate;
            }
        }

        // 3순위: 직접 연결
        return request.getRemoteAddr();
    }

    private static boolean isValidIp(String ip) {
        return IPV4.matcher(ip).matches() || IPV6.matcher(ip).matches();
    }
}
