package com.mothsoft.alexis.web.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class GoogleOauthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String GOOGLE_OAUTH_URL = "https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&access_type=offline";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        final WebApplicationContext webApplicationContext = WebApplicationContextUtils
                .getRequiredWebApplicationContext(request.getSession().getServletContext());
        final Properties properties = webApplicationContext.getBean("properties", java.util.Properties.class);

        final String clientId = properties.getProperty("google.oauth.clientId");
        final String redirectUri = URLEncoder.encode(properties.getProperty("google.oauth.redirect"), "UTF-8");
        final String scope = URLEncoder.encode(properties.getProperty("google.oauth.scope"), "UTF-8");
        final String url = String.format(GOOGLE_OAUTH_URL, clientId, redirectUri, scope);

        response.sendRedirect(url);
    }

}
