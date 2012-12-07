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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpRequestBase;

public final class HttpClientResponse implements Closeable {

    private final HttpRequestBase request;
    private final int statusCode;
    private final String etag;
    private final Date lastModifiedDate;
    private final InputStream inputStream;
    private final Charset charset;

    public HttpClientResponse(final HttpRequestBase request, final int statusCode, final String etag,
            final Date lastModifiedDate, final InputStream inputStream, final Charset charset) {
        this.request = request;
        this.statusCode = statusCode;
        this.etag = etag;
        this.lastModifiedDate = lastModifiedDate;
        this.inputStream = inputStream;
        this.charset = charset;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public InputStream getInputStream() throws IOException {
        return this.inputStream;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public String getEtag() {
        return this.etag;
    }

    public Date getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public void abort() {
        this.request.abort();
    }

    public void close() {
        IOUtils.closeQuietly(inputStream);
    }
}
