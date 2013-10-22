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
package com.mothsoft.alexis.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.log4j.Logger;

public class NetworkingUtil {

    public static final long MAX_CONTENT_LENGTH = 1024 * 1024 * 2;

    private static final Logger logger = Logger.getLogger(NetworkingUtil.class);

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public static int delete(final URL url, final CredentialsProvider credentialsProvider) {
        final HttpDelete delete = new HttpDelete(url.toExternalForm());

        // set up HTTP context
        final HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        final AuthCache authCache = new BasicAuthCache();
        final BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        final HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);
        localContext.setCredentialsProvider(credentialsProvider);
        localContext.setTargetHost(targetHost);

        final HttpClient client = getClient();
        HttpResponse response;
        try {
            response = client.execute(targetHost, delete, localContext);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int statusCode = response.getStatusLine().getStatusCode();

        return statusCode;
    }

    public static HttpClientResponse get(final URL url, final String etag, final Date lastModifiedDate)
            throws IOException {
        return get(url, etag, lastModifiedDate, null);
    }

    public static HttpClientResponse get(final URL url, final String etag, final Date lastModifiedDate,
            final CredentialsProvider credentialsProvider) throws IOException {

        final HttpGet get = new HttpGet(url.toExternalForm());

        get.addHeader("Accept-Charset", "UTF-8");

        if (etag != null) {
            get.addHeader("If-None-Match", etag);
        }

        if (lastModifiedDate != null) {
            get.addHeader("If-Modified-Since", DateUtils.formatDate(lastModifiedDate));
        }

        int statusCode = -1;
        String responseEtag = null;
        Date responseLastModifiedDate = null;

        final HttpClient client = getClient();
        HttpResponse response = null;

        if (credentialsProvider != null) {
            // set up HTTP context
            final HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
            final AuthCache authCache = new BasicAuthCache();
            final BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            final HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);
            localContext.setCredentialsProvider(credentialsProvider);
            localContext.setTargetHost(targetHost);

            response = client.execute(targetHost, get, localContext);
        } else {
            response = client.execute(get);
        }

        statusCode = response.getStatusLine().getStatusCode();

        InputStream is = null;
        Charset charset = null;

        if (statusCode == 304) {
            responseEtag = etag;
            responseLastModifiedDate = lastModifiedDate;
        } else {
            final Header responseEtagHeader = response.getFirstHeader("Etag");
            if (responseEtagHeader != null) {
                responseEtag = responseEtagHeader.getValue();
            }

            final Header lastModifiedDateHeader = response.getFirstHeader("Last-Modified");
            if (lastModifiedDateHeader != null) {
                try {
                    responseLastModifiedDate = DateUtils.parseDate(lastModifiedDateHeader.getValue());
                } catch (DateParseException e) {
                    responseLastModifiedDate = null;
                }
            }

            final HttpEntity entity = response.getEntity();

            // here's where to do intelligent checking of content type, content
            // length, etc.
            if (entity.getContentLength() > MAX_CONTENT_LENGTH) {
                get.abort();
                throw new IOException("Exceeded maximum content length, length is: " + entity.getContentLength());
            }

            is = entity.getContent();
            charset = getCharset(entity);
        }
        return new HttpClientResponse(get, statusCode, responseEtag, responseLastModifiedDate, is, charset);
    }

    public static HttpClientResponse post(final URL url, final List<NameValuePair> params) throws IOException {
        final HttpPost post = new HttpPost(url.toExternalForm());

        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
        post.setEntity(formEntity);

        post.addHeader("Accept-Charset", "UTF-8");

        final HttpClient client = getClient();
        HttpResponse response = client.execute(post);
        int status = response.getStatusLine().getStatusCode();

        if (status != 200) {
            throw new IOException("status: " + status);
        }

        final HttpEntity entity = response.getEntity();
        final InputStream is = entity.getContent();
        final Charset charset = getCharset(entity);

        return new HttpClientResponse(post, status, null, null, is, charset);
    }

    public static HttpClientResponse post(final URL url, final String content, final String mimeType,
            final CredentialsProvider credentialsProvider) throws IOException {
        final HttpPost post = new HttpPost(url.toExternalForm());
        post.setEntity(new StringEntity(content));
        post.setHeader("Content-Type", mimeType);
        return NetworkingUtil.execute(url, post, credentialsProvider);
    }

    public static HttpClientResponse put(final URL url, final String content, final String mimeType,
            final CredentialsProvider credentialsProvider) throws IOException {
        final HttpPut put = new HttpPut(url.toExternalForm());
        put.setEntity(new StringEntity(content));
        put.setHeader("Content-Type", mimeType);
        return NetworkingUtil.execute(url, put, credentialsProvider);
    }

    private static HttpClientResponse execute(final URL url, final HttpEntityEnclosingRequest method,
            final CredentialsProvider credentialsProvider) throws IOException {

        // set up HTTP context
        final HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        final AuthCache authCache = new BasicAuthCache();
        final BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        final HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);
        localContext.setCredentialsProvider(credentialsProvider);
        localContext.setTargetHost(targetHost);

        final HttpClient client = getClient();

        HttpResponse response = client.execute(targetHost, method, localContext);
        int status = response.getStatusLine().getStatusCode();

        if (status < 200 || status > 300) {
            throw new IOException("status: " + status);
        }

        final HttpEntity entity = response.getEntity();
        final InputStream is = entity.getContent();
        final Charset charset = getCharset(entity);

        return new HttpClientResponse((AbortableHttpRequest)method, status, null, null, is, charset);
    }

    private static HttpClient getClient() {
        final HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
        client.getParams().setParameter("http.socket.timeout", new Integer(300000));
        client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        return client;
    }

    private static Charset getCharset(final HttpEntity entity) {
        final Header encodingHeader = entity.getContentEncoding();
        final String encoding = encodingHeader == null ? null : encodingHeader.getValue();

        if (encoding != null && Charset.isSupported(encoding)) {
            return Charset.forName(encoding);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Charset: " + encoding + " is not supported");
            }
        }

        return DEFAULT_CHARSET;
    }

}
