package com.rehancode.finpay.Util;

import com.rehancode.finpay.Exception.UserNotAuthenticated;
import com.rehancode.finpay.SecurityConfig.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@Slf4j
public class SecurityUtil {

    private SecurityUtil() {
        // static utility – no instantiation
    }

    /**
     * Returns the ID of the currently authenticated user.
     *
     * @throws UserNotAuthenticated if there is no valid-authenticated principal in the context
     */
    public static Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null ||
                !auth.isAuthenticated() ||
                auth.getPrincipal() == null ||
                "anonymousUser".equals(auth.getPrincipal())) {

            log.error("User is not authenticated");
            throw new UserNotAuthenticated("User not authenticated");

        }

        if (!(auth.getPrincipal() instanceof UserDetailsImpl userPrincipal)) {
            log.error("Invalid user principal");
            throw new UserNotAuthenticated("Invalid user principal");
        }

        return userPrincipal.getUser().getId();
    }
}