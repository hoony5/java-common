package com.deoksan.share.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator implementation for {@link Slug} annotation.
 */
public class SlugValidator implements ConstraintValidator<Slug, String> {

    private static final Pattern SLUG_PATTERN  = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final int    MAX_SLUG_LENGTH = 100;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle null/empty separately
        }
        return value.length() <= MAX_SLUG_LENGTH && SLUG_PATTERN.matcher(value).matches();
    }
}
