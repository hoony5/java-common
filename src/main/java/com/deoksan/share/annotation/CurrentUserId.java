package com.deoksan.share.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that resolves the authenticated user's ID from the request.
 *
 * Works with:
 * - OAuth2 login: extracts user ID from Authentication.getPrincipal()
 * - JWT token: extracts subject claim from the token
 * - API key: resolves user from the key header
 *
 * Usage:
 * <pre>
 * {@code @GetMapping("/posts")}
 * public PageResponse<PostDto> getPosts(@CurrentUserId String userId) { ... }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}
