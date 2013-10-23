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
package com.mothsoft.alexis.engine.textual;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.BoilerpipeContentHandler;
import org.apache.tika.parser.html.HtmlParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.l3s.boilerpipe.extractors.ArticleSentencesExtractor;
import de.l3s.boilerpipe.extractors.KeepEverythingExtractor;

public class WebContentParserImpl implements WebContentParser {

    private org.apache.tika.parser.AutoDetectParser autoDetectParser;
    private Detector detector;

    private static final Set<MediaType> HTML_TYPES = Collections.unmodifiableSet(new HashSet<MediaType>(Arrays.asList(
            MediaType.text("html"), MediaType.application("xhtml+xml"), MediaType.application("vnd.wap.xhtml+xml"),
            MediaType.application("x-asp"))));

    public WebContentParserImpl() {
        this.autoDetectParser = new AutoDetectParser();
        this.detector = new DefaultDetector();
    }

    public String parse(final InputStream is) throws IOException {
        final InputStream bufferedStream = buffered(is);

        final StringBuffer buffer = new StringBuffer();
        final org.apache.tika.mime.MediaType mediaType = this.detector.detect(bufferedStream, new Metadata());

        final ContentHandler handler;
        if (HTML_TYPES.contains(mediaType)) {
            // if coming in as a stream and HTML, likely part of a larger
            // document (web page), we would like to do article extraction
            // FIXME - smarter handler?
            handler = new BoilerpipeContentHandler(new FullTextContentHandler(buffer),
                    ArticleSentencesExtractor.INSTANCE);
        } else {
            // assuming full documents like Word or PDF are more about a single
            // topic
            handler = new FullTextContentHandler(buffer);
        }

        return parse(this.autoDetectParser, bufferedStream, handler, buffer);
    }

    private BufferedInputStream buffered(InputStream is) {
        return new BufferedInputStream(is, 1024 * 16);
    }

    public String parseHTML(final String string) throws IOException {
        final StringBuffer buffer = new StringBuffer();
        final HtmlParser htmlParser = new HtmlParser();
        final BoilerpipeContentHandler handler = new BoilerpipeContentHandler(new FullTextContentHandler(buffer),
                KeepEverythingExtractor.INSTANCE);
        return parse(htmlParser, new ReaderInputStream(new StringReader(string)), handler, buffer);
    }

    private String parse(org.apache.tika.parser.Parser parser, InputStream is, ContentHandler handler,
            StringBuffer buffer) throws IOException {
        final Metadata metadata = new Metadata();
        final ParseContext context = new ParseContext();

        try {
            parser.parse(is, handler, metadata, context);
            return StringUtils.trimToEmpty(buffer.toString());
        } catch (SAXException e) {
            throw new IOException(e.getLocalizedMessage());
        } catch (TikaException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    private class FullTextContentHandler extends DefaultHandler {
        private StringBuffer buffer;
        private boolean lastWasWhitespace = false;

        FullTextContentHandler(final StringBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            buffer.append(chars, start, length);
            lastWasWhitespace = false;
        }

        @Override
        public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
            if (!lastWasWhitespace) {
                buffer.append(" ");
                lastWasWhitespace = true;
            }
        }

    }
}
