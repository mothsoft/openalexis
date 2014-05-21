/*   Copyright 2012 Tim Garrett, Mothsoft LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mothsoft.alexis.security;

import java.util.TimeZone;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mothsoft.alexis.domain.UserAuthenticationDetails;

public final class CurrentUserUtil {

    public static void assertAuthenticatedUserOrAdminOrSystem(final Long userId) {
        if (!isAuthenticated() || !getCurrentUserId().equals(userId)) {
            throw new AccessDeniedException(String.format("User %s lacks necessary permissions.", getCurrentUser()
                    .getUsername()));
        }
    }

    public static boolean isAuthenticated() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
    }

    public static UserAuthenticationDetails getCurrentUser() {
        final SecurityContext ctx = SecurityContextHolder.getContext();
        final Authentication authentication = ctx.getAuthentication();

        try {
            return authentication != null && authentication.isAuthenticated() ? (UserAuthenticationDetails) authentication
                    .getPrincipal() : null;
        } catch (ClassCastException e) {
            throw new AuthenticationServiceException(e.getLocalizedMessage(), e);
        }
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    public static TimeZone getTimeZone() {
        return getCurrentUser().getTimeZone();
    }

    public static boolean isAdmin() {
        return getCurrentUser().isAdmin();
    }

    public static boolean isSystem() {
        return getCurrentUser().isSystem();
    }

    public static void setSystemUserAuthentication() {
        final UserAuthenticationDetails systemUser = new UserAuthenticationDetails(true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(systemUser, null, UserAuthenticationDetails.ADMIN_AUTHORITIES));
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
