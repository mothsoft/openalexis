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

public enum PartOfSpeech {

    CC(1),
    CD(2),
    DT(3),
    EX(4),
    FW(5),
    IN(6),
    JJ(7),
    JJR(8),
    JJS(9),
    LS(10),
    MD(11),
    NN(12),
    NNS(13),
    NNP(14),
    NNPS(15),
    NP(16),
    PDT(17),
    POS(18),
    PRP(19),
    PRP$(20),
    RB(21),
    RBR(22),
    RBS(23),
    RP(24),
    SYM(25),
    TO(26),
    UH(27),
    VB(28),
    VBD(29),
    VBG(30),
    VBN(31),
    VBP(32),
    VBZ(33),
    WDT(34),
    WHNP(35),
    WP(36),
    WP$(37),
    WRB(38),
    PUNCTUATION(39),
    UNKNOWN(40);

    private static Map<Integer, PartOfSpeech> map;

    static {
        map = new HashMap<Integer, PartOfSpeech>(128);

        for (final PartOfSpeech ith : PartOfSpeech.values()) {
            map.put(ith.getValue(), ith);
        }
    }

    private int value;

    private PartOfSpeech(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static PartOfSpeech getByValue(final int value) {
        return map.get(value);
    }

    public static PartOfSpeech parse(String posString) {
        try {
            return PartOfSpeech.valueOf(posString);
        } catch (final IllegalArgumentException e) {
            return PartOfSpeech.UNKNOWN;
        }
    }

}
