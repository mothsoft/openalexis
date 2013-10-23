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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Test;

public class WebContentParserTest {

    private WebContentParser wcp = new WebContentParserImpl();

    @Test
    public void testParseInputStreamText() throws IOException {
        final String text = "Hello, I am a document.";
        final InputStream is = new ByteArrayInputStream(text.getBytes(Charset.forName("UTF-8")));
        assertEquals("Hello, I am a document.", wcp.parse(is));
    }

    // FIXME - Boilerpipe isn't doing that good of job. The first sentence of
    // this article doesn't even make it in with the ArticleExtractor. Consider
    // writing a stack-based parser that tracks probable content tags and
    // discards on inferred HTML semantic structure rather than trying to do it
    // with Boilerpipe's algorithms
    @Test
    public void testParseInputStreamHTML() throws IOException {
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream("test-article.html");
        final String document = wcp.parse(is);
        System.out.println(document);
        assertTrue(document.contains("including the self-proclaimed mastermind"));
    }

    @Test
    public void testParseHTML() throws IOException {
        final String html = "I hate <b>HTML</b> when I am expecting <em>only</em> plain text.";
        assertEquals("I hate HTML when I am expecting only plain text.", wcp.parseHTML(html));
    }

}
