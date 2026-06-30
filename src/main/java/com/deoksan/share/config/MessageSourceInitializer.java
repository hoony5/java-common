package com.deoksan.share.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * MessageSource를 AssembledErrorCode에 주입한다.
 */
@Component
@RequiredArgsConstructor
public class MessageSourceInitializer {

    private final MessageSource messageSource;

    @PostConstruct
    public void init() {
        com.deoksan.share.exception.AssembledErrorCode.setMessageSource(messageSource);
    }
}
