package com.deoksan.share.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class Messages {

    private static MessageSource source;

    public Messages(MessageSource messageSource) {
        Messages.source = messageSource;
    }

    public static String get(String key) {
        return source.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }

    public static String get(String key, Object... args) {
        return source.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
