package com.deoksan.share.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a string is a valid slug (URL-friendly identifier).
 *
 * A valid slug:
 * - Contains only lowercase letters, numbers, and hyphens
 * - Does not start or end with a hyphen
 * - Is between 1 and 100 characters
 *
 * Usage:
 * <pre>
 * {@code @Slug}
 * private String slug;
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SlugValidator.class)
public @interface Slug {

    String message() default "{validation.post.slug.format}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
