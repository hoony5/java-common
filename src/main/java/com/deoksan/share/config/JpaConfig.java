package com.deoksan.share.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA 설정.
 *
 * - @EnableJpaAuditing: 엔티티의 @CreatedDate / @LastModifiedDate 활성화
 * - @EnableJpaRepositories: 리포지토리 스캔 범위를 명시적으로 지정
 * - ddl-auto=validate (application.properties): Flyway가 스키마를 소유, Hibernate는 유효성만 검사
 *
 * 스캔 패키지는 @SpringBootApplication의 basePackages에서 자동으로 파생된다.
 * 패키지를 변경하려면 PersonalBlogApplication의 @SpringBootApplication 인자를 수정하면 된다.
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories
public class JpaConfig {
}
