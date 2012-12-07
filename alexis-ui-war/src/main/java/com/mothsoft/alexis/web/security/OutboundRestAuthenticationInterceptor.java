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
package com.mothsoft.alexis.web.security;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.jaxrs.interceptor.JAXRSOutInterceptor;
import org.apache.cxf.message.Message;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import com.mothsoft.alexis.security.CurrentUserUtil;

public class OutboundRestAuthenticationInterceptor extends JAXRSOutInterceptor {

    @Override
    public void handleMessage(Message message) {

        if (!CurrentUserUtil.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Unauthenticated user!");
        }

        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
        if (headers == null) {
            headers = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
            message.put(Message.PROTOCOL_HEADERS, headers);
        }

        final List<String> header = new ArrayList<String>();

        final String username = CurrentUserUtil.getCurrentUser().getUsername();
        final String apiToken = CurrentUserUtil.getCurrentUser().getApiToken();
        final String usernameToken = String.format("%s:%s", username, apiToken);
        try {
            header.add(String.format("Basic %s", new String(Base64.encodeBase64(usernameToken.getBytes("UTF-8")))));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        headers.put("Authorization", header);
    }
}
