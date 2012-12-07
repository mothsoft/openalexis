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
package com.mothsoft.alexis.service.monitoring;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class RequestTimingFilter implements Filter {

    private static final Logger logger = Logger.getLogger(RequestTimingFilter.class);

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        Long start = -1L;

        if (logger.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }

        try {
            chain.doFilter(request, response);
        } finally {
            if (logger.isDebugEnabled()) {
                final Long elapsed = System.currentTimeMillis() - start;
                logger.debug("Request: " + httpRequest.getRequestURI() + ", elapsed time: " + elapsed + " ms.");
            }
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

}
