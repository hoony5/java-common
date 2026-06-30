package com.deoksan.share.config;

import com.deoksan.share.config.TtlConstants;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * 다국어(i18n) 설정.
 *
 * <p>클라이언트의 언어 설정에 따라 에러 메시지를 번역한다.
 * 언어는 쿠키에 저장되며, {@code ?lang=ko} 또는 {@code ?lang=en}으로 변경 가능.
 *
 * <h3>사용 예</h3>
 * <pre>
 * // 기본: 브라우저 Locale
 * GET /api/auth/me
 * → 한국어 메시지 반환
 *
 * // 언어 변경
 * GET /api/auth/me?lang=en
 * → 영어 메시지 반환 (쿠키에 저장)
 * </pre>
 */
@Configuration
public class I18nConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:i18n/messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("lang");
        // 쿠키 없을 시 브라우저 Accept-Language 헤더 사용
        resolver.setDefaultLocale(null);
        resolver.setCookieMaxAge(TtlConstants.LOCALE_COOKIE);
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Bean Validation이 MessageSource를 사용하여
     * 어노테이션 message를 i18n 키로 해결하도록 설정.
     */
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(messageSource);
        return factory;
    }
}
