package com.yusufjon.recruitmentplatform.support;

/**
 * Provides small test helpers for populating and clearing the Spring Security context in unit
 * tests.
 */

import com.yusufjon.recruitmentplatform.user.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

public final class SecurityTestUtils {

    private SecurityTestUtils() {
    }

    public static void authenticate(User user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
