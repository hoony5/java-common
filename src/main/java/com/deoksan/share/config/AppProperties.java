package com.deoksan.share.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 전역 애플리케이션 설정 프로퍼티.
 * application.properties 또는 환경변수에서 값을 읽어온다.
 *
 * 프로퍼티 키는 모두 "app." 접두사를 사용한다.
 *
 * 사용 예:
 * - app.author-id=hoony5
 * - app.base-url=https://myblog.com
 * - app.cors-allowed-origins=http://localhost:3000,https://myblog.com
 *
 * 환경변수 매핑:
 * - APP_AUTHOR_ID → app.author-id
 * - APP_BASE_URL → app.base-url
 */
@Getter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** 작성자(운영자) ID. 기본값: admin */
    private String authorId = "admin";

    /** 작성자(운영자) 표시 이름. 기본값: Blog Admin */
    private String authorName = "Blog Admin";

    /** 블로그 베이스 URL. Swagger, 링크 생성 등에 사용. */
    private String baseUrl = "http://localhost:8080";

    /** GitHub 프로필 URL */
    private String githubUrl;

    /** 블로그 로컬 도메인 이름 (프로젝트 패키지명) */
    private String basePackage = "com.deoksan";

    /** CORS 허용 오리진 목록. application.properties에서 쉼표로 구분. */
    private List<String> corsAllowedOrigins = List.of(
            "http://localhost:3000",
            "http://localhost:5173"
    );

    /** Swagger/OpenAPI 문서 제목 */
    private String swaggerTitle = "Personal Blog API";

    /** Swagger/OpenAPI 문서 설명 */
    private String swaggerDescription = "REST API for a personal blog platform with OAuth2 authentication";

    /** Swagger/OpenAPI 문서 버전 */
    private String swaggerVersion = "v1.0.0";

    /** 서버 포트 (헬스체크 URL 생성용) */
    private int serverPort = 8080;

    /** 더미 데이터 기본 작성자 ID (별도 지정 없으면 authorId 사용) */
    private String dummyAuthorId;

    /**
     * 더미 데이터 작성자 Long ID.
     * Post.authorId가 Long이므로 숫자형 ID를 별도로 설정한다.
     * 기본값 1L — 개발 DB의 첫 번째 사용자(시드 어드민)를 의미한다.
     */
    private Long dummyAuthorLongId = 1L;

    public String getDummyAuthorId() {
        return dummyAuthorId != null ? dummyAuthorId : authorId;
    }

    public Long getDummyAuthorLongId() {
        return dummyAuthorLongId;
    }

    /**
     * Swagger UI 전체 URL을 반환한다.
     */
    public String getSwaggerUiUrl() {
        return baseUrl + "/swagger-ui.html";
    }

    /**
     * Actuator 헬스체크 URL을 반환한다.
     */
    public String getHealthCheckUrl() {
        return baseUrl + "/actuator/health";
    }
}
