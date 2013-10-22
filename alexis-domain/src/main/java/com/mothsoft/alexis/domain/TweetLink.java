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
package com.mothsoft.alexis.domain;


public class TweetLink {

    private short start;

    private short end;

    private String displayUrl;

    private String expandedUrl;

    private String url;

    public TweetLink(short start, short end, String displayUrl, String expandedUrl, String url) {
        this.start = start;
        this.end = end;
        this.displayUrl = displayUrl;
        this.expandedUrl = expandedUrl;
        this.url = url;
    }

    protected TweetLink() {
    }

    public short getStart() {
        return start;
    }

    public short getEnd() {
        return end;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public String getExpandedUrl() {
        return expandedUrl;
    }

    public String getUrl() {
        return url;
    }

}
