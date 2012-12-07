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

public enum AssociationType {
    // as defined here: http://nlp.stanford.edu/pubs/LREC06_dependencies.pdf
    root(0),
    abbrev(1),
    acomp(2),
    advmod(3),
    advcl(4),
    agent(5),
    amod(6),
    appos(7),
    attr(8),
    aux(9),
    auxpass(10),
    cc(11),
    ccomp(12),
    complm(13),
    conj(14),
    conj_and(15),
    conj_but(16),
    conj_negcc(17),
    conj_nor(18),
    conj_or(19),
    csubj(20),
    csubjpass(21),
    comp(22),
    compl(23),
    cop(24),
    dep(25),
    det(26),
    dobj(27),
    expl(28),
    infmod(29),
    iobj(30),
    mark(31),
    measure(32),
    mod(33),
    neg(34),
    nn(35),
    nsubj(36),
    nsubjpass(37),
    num(38),
    number(39),
    obj(40),
    parataxis(41),
    partmod(42),
    pobj(43),
    poss(44),
    possessive(45),
    preconj(46),
    predet(47),
    prep(48),
    prepc(49),
    prt(50),
    punct(51),
    purpcl(52),
    quantmod(53),
    rcmod(54),
    ref(55),
    rel(56),
    sdep(57),
    tmod(58),
    xcomp(59),
    xsub(60),
    pcomp(61),
    pred(62),
    mwe(63),
    npadvmod(64);

    private static Map<Integer, AssociationType> map;

    static {
        map = new HashMap<Integer, AssociationType>(128);

        for (final AssociationType ith : AssociationType.values()) {
            map.put(ith.getValue(), ith);
        }
    }

    private int value;

    private AssociationType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static AssociationType getByValue(int value) {
        return map.get(value);
    }
}