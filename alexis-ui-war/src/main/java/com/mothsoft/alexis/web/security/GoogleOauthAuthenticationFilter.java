package com.mothsoft.alexis.web.security;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.util.HttpClientResponse;
import com.mothsoft.alexis.util.NetworkingUtil;

public class GoogleOauthAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String CODE_EXCHANGE_URL = "https://accounts.google.com/o/oauth2/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Properties properties;

    public GoogleOauthAuthenticationFilter(final Properties properties) {
        super("/profiles/google/oauth");
        this.properties = properties;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        if (request.getSession(false) == null) {
            throw new BadCredentialsException("Existing session missing.");
        }

        final String code = request.getParameter("code");
        final URL url = new URL(CODE_EXCHANGE_URL);

        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("code", code));
        params.add(new BasicNameValuePair("client_id", this.properties.getProperty("google.oauth.clientId")));
        params.add(new BasicNameValuePair("client_secret", this.properties.getProperty("google.oauth.clientSecret")));
        params.add(new BasicNameValuePair("redirect_uri", this.properties.getProperty("google.oauth.redirect")));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));

        final HttpClientResponse tokenResponse = NetworkingUtil.post(url, params);
        final Map<String, Object> tokenDetails = OBJECT_MAPPER.readValue(tokenResponse.getInputStream(), Map.class);
        final String accessToken = (String) tokenDetails.get("access_token");
        final String refreshToken = (String) tokenDetails.get("refresh_token");

        final Map<String, Object> userDetails = getUserDetails(accessToken);
        final String username = (String) userDetails.get("id");
        final String email = (String) userDetails.get("email");

        return new OauthAuthenticationToken(username, email, accessToken, refreshToken);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getUserDetails(String accessToken) throws IOException {
        final URL url = new URL(String.format(USER_INFO_URL, accessToken));
        final HttpClientResponse clientResponse = NetworkingUtil.get(url, null, null);
        final Map<String, Object> userDetails = OBJECT_MAPPER.readValue(clientResponse.getInputStream(), Map.class);
        return userDetails;
    }

    public static class OauthAuthenticationToken extends AbstractAuthenticationToken {

        private static final long serialVersionUID = 1L;

        private String username;
        private String email;
        private String accessToken;
        private String refreshToken;

        public OauthAuthenticationToken(String username, String email, String accessToken, String refreshToken) {
            super(createUserAuthorities());
            this.username = username;
            this.email = email;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        private static Collection<GrantedAuthority> createUserAuthorities() {
            final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
            return authorities;
        }

        @Override
        public Object getCredentials() {
            return this.accessToken;
        }

        @Override
        public Object getPrincipal() {
            return this.username;
        }

        public String getUsername() {
            return this.username;
        }

        public String getEmail() {
            return this.email;
        }

        public String getAccessToken() {
            return this.accessToken;
        }

        public String getRefreshToken() {
            return this.refreshToken;
        }

    }

}
