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
package com.mothsoft.alexis.web;

import java.nio.charset.Charset;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.springframework.security.core.GrantedAuthority;

import com.mothsoft.alexis.security.CurrentUserUtil;

public class CurrentUser {

    public boolean isAuthenticated() {
        return CurrentUserUtil.isAuthenticated();
    }

    public String getUsername() {
        return CurrentUserUtil.getCurrentUser().getUsername();
    }

    public String getApiToken() {
        return CurrentUserUtil.getCurrentUser().getApiToken();
    }

    public String getApiBasicAuthorizationHeaderValue() {
        final byte[] bytes = new String(getUsername() + ":" + getApiToken()).getBytes(Charset.forName("UTF-8"));
        return "Basic " + Base64.encodeBase64String(bytes);
    }

    public TimeZone getTimeZone() {
        return CurrentUserUtil.getTimeZone();
    }

    public boolean isHasAnalysisRole() {
        for (final GrantedAuthority authority : CurrentUserUtil.getCurrentUser().getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_ANALYSIS")) {
                return true;
            }
        }
        return false;
    }

}
