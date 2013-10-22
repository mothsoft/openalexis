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


public class TweetMention {

    private short start;

    private short end;

    private Long userId;

    private String name;

    private String screenName;

    public TweetMention(short start, short end, Long userId, String name, String screenName) {
        this.start = start;
        this.end = end;
        this.userId = userId;
        this.name = name;
        this.screenName = screenName;
    }

    protected TweetMention() {
    }

    public short getStart() {
        return start;
    }

    public short getEnd() {
        return end;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

}
