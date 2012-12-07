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

import java.util.HashSet;
import java.util.Set;

public class StopWords {

    public static final Set<String> ENGLISH;

    static {
        ENGLISH = new HashSet<String>(128);
        ENGLISH.add("i");
        ENGLISH.add("me");
        ENGLISH.add("my");
        ENGLISH.add("you");
        ENGLISH.add("your");
        ENGLISH.add("it");
        ENGLISH.add("its");
        ENGLISH.add("it's");
        ENGLISH.add("his");
        ENGLISH.add("her");
        ENGLISH.add("he");
        ENGLISH.add("she");
        ENGLISH.add("we");
        ENGLISH.add("they");
        ENGLISH.add("is");
        ENGLISH.add("are");
        ENGLISH.add("be");
        ENGLISH.add("been");
        ENGLISH.add("have");
        ENGLISH.add("had");
        ENGLISH.add("http");
        ENGLISH.add("t.co");
        ENGLISH.add("rt");
        ENGLISH.add("so");
        ENGLISH.add("and");
        ENGLISH.add("but");
        ENGLISH.add("for");
        ENGLISH.add("nor");
        ENGLISH.add("neither");
        ENGLISH.add("either");
        ENGLISH.add("out");
        ENGLISH.add("in");
        ENGLISH.add("here");
        ENGLISH.add("here's");
        ENGLISH.add("about");
        ENGLISH.add("by");
        ENGLISH.add("of");
        ENGLISH.add("from");
        ENGLISH.add("a");
        ENGLISH.add("an");
        ENGLISH.add("the");
        ENGLISH.add("their");
        ENGLISH.add("on");
        ENGLISH.add("at");
        ENGLISH.add("with");
        ENGLISH.add(",");
        ENGLISH.add(".");
        ENGLISH.add(":");
        ENGLISH.add(";");
        ENGLISH.add("?");
        ENGLISH.add("!");
        ENGLISH.add("-");
        ENGLISH.add("--");
        ENGLISH.add("'s");
        ENGLISH.add("'");
        ENGLISH.add("\"");
        ENGLISH.add("`");
        ENGLISH.add("``");
        ENGLISH.add("''");
    }
}
