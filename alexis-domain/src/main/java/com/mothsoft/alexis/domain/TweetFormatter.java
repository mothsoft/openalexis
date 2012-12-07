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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class TweetFormatter {

    private static final Logger logger = Logger.getLogger(TweetFormatter.class);

    public static String format(final Tweet tweet) {
        final Map<Short, Object> entities = new HashMap<Short, Object>();

        for (final TweetHashtag hashtag : tweet.getHashtags()) {
            entities.put(hashtag.getStart(), hashtag);
        }

        for (final TweetLink link : tweet.getLinks()) {
            entities.put(link.getStart(), link);
        }

        for (final TweetMention mention : tweet.getMentions()) {
            entities.put(mention.getStart(), mention);
        }

        final StringBuilder builder = new StringBuilder();
        final String text = tweet.getText();

        final int length = text.length();

        int i = 0;
        while (i < length) {
            final char ch = text.charAt(i);

            if (logger.isDebugEnabled()) {
                logger.debug("Position: " + i + " , Char: " + ch);
            }

            final Object entity = entities.get((short) i);

            if (entity == null) {
                builder.append(ch);
                i++;
            } else {
                i = handleEntity(text, entity, builder);
            }
        }

        return builder.toString();
    }

    private static int handleEntity(String text, Object entity, StringBuilder builder) {
        if (entity instanceof TweetHashtag) {
            return handleHashtag(text, (TweetHashtag) entity, builder);
        } else if (entity instanceof TweetLink) {
            return handleLink(text, (TweetLink) entity, builder);
        } else if (entity instanceof TweetMention) {
            return handleMention(text, (TweetMention) entity, builder);
        } else {
            throw new IllegalStateException("Unexpected entity type found: " + entity.getClass().getName());
        }
    }

    private static int handleLink(String text, TweetLink link, StringBuilder builder) {
        String linkUrl = link.getExpandedUrl() != null ? link.getExpandedUrl() : link.getUrl();
        String displayUrl = link.getDisplayUrl() != null ? link.getDisplayUrl() : link.getUrl();

        if (displayUrl == null) {
            displayUrl = text.substring(link.getStart(), link.getEnd());
            linkUrl = displayUrl.startsWith("http://") || displayUrl.startsWith("https://") ? displayUrl : "http://"
                    + displayUrl;
        }

        builder.append(url(linkUrl, displayUrl));

        return (int) link.getEnd();
    }

    private static int handleMention(String text, TweetMention mention, StringBuilder builder) {
        builder.append(url("http://twitter.com/" + mention.getScreenName(), "@" + mention.getScreenName()));
        return (int) mention.getEnd();
    }

    private static int handleHashtag(String text, TweetHashtag hashtag, StringBuilder builder) {
        builder.append(url("http://twitter.com/search?q=%23" + hashtag.getHashtag(), "#" + hashtag.getHashtag()));
        return (int) hashtag.getEnd();
    }

    private static String url(String linkUrl, String displayText) {
        return ("<a href=\"" + linkUrl + "\" target=\"_blank\">" + displayText + "</a>");
    }
}
