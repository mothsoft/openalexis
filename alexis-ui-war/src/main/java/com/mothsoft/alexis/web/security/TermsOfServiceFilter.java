/*   Copyright 2013 Tim Garrett, Mothsoft LLC
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
package com.mothsoft.alexis.web.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mothsoft.alexis.domain.User;
import com.mothsoft.alexis.domain.UserAuthenticationDetails;
import com.mothsoft.alexis.service.UserService;

public class TermsOfServiceFilter extends OncePerRequestFilter {

    private static final String ALEXIS_CSS_PREFIX = "/alexis/css/";
    private static final String ALEXIS_IMAGES_PREFIX = "/alexis/images/";
    private static final String ALEXIS_JS_PREFIX = "/alexis/js/";
    private static final String ANONYMOUS_USER = "anonymousUser";
    private static final String TOS_KEY = "tosAcceptDate";
    private final String TERMS_OF_SERVICE_URI = "/alexis/terms-of-service/";

    private UserService userService;

    public TermsOfServiceFilter(UserService userService) {
        this.userService = userService;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final HttpSession session = request.getSession(false);

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || ANONYMOUS_USER.equals(authentication.getName()) || isStaticContent(request)) {
            chain.doFilter(request, response);
            return;
        }

        if (session.getAttribute(TOS_KEY) == null) {
            final UserAuthenticationDetails details = (UserAuthenticationDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            final Long userId = details.getUserId();
            final User user = this.userService.getUser(userId);

            if (user.getTosAcceptDate() != null) {
                session.setAttribute(TOS_KEY, user.getTosAcceptDate());
            }
        }

        if (session.getAttribute(TOS_KEY) == null && !request.getRequestURI().equals(TERMS_OF_SERVICE_URI)) {
            response.sendRedirect(TERMS_OF_SERVICE_URI);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isStaticContent(HttpServletRequest request) {
        final String requestUri = request.getRequestURI();
        return requestUri.startsWith(ALEXIS_CSS_PREFIX) || requestUri.startsWith(ALEXIS_IMAGES_PREFIX)
                || requestUri.startsWith(ALEXIS_JS_PREFIX);
    }
}
