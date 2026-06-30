package com.deoksan.share.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/i18n")
@RequiredArgsConstructor
public class I18nController {

    private static final String KEY_LANG = "lang";
    private static final String KEY_DISPLAY = "display";
    private static final String KEY_NAME = "name";
    private static final String KEY_MESSAGE = "message";

    private static final String LANG_KO = "ko";
    private static final String LANG_EN = "en";
    private static final String LANG_JA = "ja";
    private static final String LANG_ZH = "zh";

    private static final String MSG_LANG_CHANGED = "i18n.lang.changed";

    private final LocaleResolver localeResolver;
    private final MessageSource messageSource;

    @GetMapping("/current")
    public Map<String, String> currentLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        return Map.of(
                KEY_LANG, locale.getLanguage(),
                KEY_DISPLAY, locale.getDisplayName(locale)
        );
    }

    @PostMapping("/set")
    public Map<String, String> setLocale(@RequestParam String lang, HttpServletResponse response) {
        Locale locale = resolveLocale(lang);
        CookieLocaleResolver cookieResolver = (CookieLocaleResolver) localeResolver;
        cookieResolver.setLocale(null, response, locale);
        String message = messageSource.getMessage(MSG_LANG_CHANGED, null, locale);
        return Map.of(
                KEY_LANG, locale.getLanguage(),
                KEY_DISPLAY, locale.getDisplayName(locale),
                KEY_MESSAGE, message
        );
    }

    @GetMapping("/supported")
    public List<Map<String, String>> supportedLocales() {
        return List.of(
                Map.of(KEY_LANG, LANG_KO, KEY_NAME, "한국어"),
                Map.of(KEY_LANG, LANG_EN, KEY_NAME, "English"),
                Map.of(KEY_LANG, LANG_JA, KEY_NAME, "日本語"),
                Map.of(KEY_LANG, LANG_ZH, KEY_NAME, "中文")
        );
    }

    private Locale resolveLocale(String lang) {
        return switch (lang.toLowerCase()) {
            case LANG_EN -> Locale.ENGLISH;
            case LANG_JA -> Locale.JAPANESE;
            case LANG_ZH -> Locale.CHINESE;
            default -> Locale.KOREAN;
        };
    }
}
