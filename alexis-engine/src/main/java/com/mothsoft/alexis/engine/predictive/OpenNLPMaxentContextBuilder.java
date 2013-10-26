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
package com.mothsoft.alexis.engine.predictive;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentAssociation;
import com.mothsoft.alexis.domain.DocumentNamedEntity;
import com.mothsoft.alexis.domain.DocumentTerm;

public class OpenNLPMaxentContextBuilder {

    private static final String HOUR_OF_DAY_FORMAT = "HOUR_OF_DAY=%d";
    private static final String QUARTER_OF_HOUR_FORMAT = "QTR_OF_HOUR=%d";
    private static final String DAY_OF_WEEK_FORMAT = "DAY_OF_WEEK=%d";
    private static final String DAY_OF_MONTH_FORMAT = "DAY_OF_MONTH=%d";
    private static final String ASSOC_FORMAT = "%s:%s";

    /** Build a context map from a document */
    public static Map<String, Integer> buildContext(final Document document) {
        final Map<String, Integer> contextMap = new LinkedHashMap<String, Integer>(512);

        for (final DocumentTerm dt : document.getDocumentTerms()) {
            final String value = dt.getTerm().getValueLowercase();
            putAndIncrement(contextMap, value, dt.getCount());
        }

        for (final DocumentAssociation association : document.getDocumentAssociations()) {
            final String value = String.format(ASSOC_FORMAT, association.getA().getValueLowercase(), association.getB()
                    .getValueLowercase());
            putAndIncrement(contextMap, value, association.getCount());
        }

        for (final DocumentNamedEntity name : document.getDocumentNamedEntities()) {
            final String value = name.getName();
            putAndIncrement(contextMap, value, name.getCount());
        }

        // build time-oriented context features
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(document.getCreationDate());

        final String hourOfDay = String.format(HOUR_OF_DAY_FORMAT, calendar.get(Calendar.HOUR_OF_DAY));
        putAndIncrement(contextMap, hourOfDay, 1);

        final String quarterOfHour = String.format(QUARTER_OF_HOUR_FORMAT,
                getQuarterOfHour(calendar.get(Calendar.MINUTE)));
        putAndIncrement(contextMap, quarterOfHour, 1);

        final String dayOfWeek = String.format(DAY_OF_WEEK_FORMAT, calendar.get(Calendar.DAY_OF_WEEK));
        putAndIncrement(contextMap, dayOfWeek, 1);

        final String dayOfMonth = String.format(DAY_OF_MONTH_FORMAT, calendar.get(Calendar.DAY_OF_MONTH));
        putAndIncrement(contextMap, dayOfMonth, 1);

        return contextMap;
    }

    /**
     * Qtr of hour: [0, 14] = 0, [15, 29] = 1, [30, 44] = 2, [45, 59] = 3
     * 
     * @param i
     * @return
     */
    private static int getQuarterOfHour(final int minute) {
        if (minute <= 14) {
            return 0;
        } else if (minute <= 29) {
            return 1;
        } else if (minute <= 44) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Append the context from 'document' to the supplied context map
     * 'contextMap'
     */
    public static void append(final Map<String, Integer> contextMap, final Document document) {
        final Map<String, Integer> newMap = buildContext(document);

        for (final Map.Entry<String, Integer> newEntry : newMap.entrySet()) {
            final String key = newEntry.getKey();
            final Integer value = newEntry.getValue();
            putAndIncrement(contextMap, key, value);
        }
    }

    public static void buildContextArrays(final Map<String, Integer> contextMap, final String[] context,
            final float[] values) {
        int i = 0;
        for (final Map.Entry<String, Integer> ith : contextMap.entrySet()) {
            context[i] = ith.getKey();
            values[i] = ith.getValue();
            i++;
        }
    }

    private static void putAndIncrement(final Map<String, Integer> map, final String value, final int count) {
        int newCount = count;

        if (map.containsKey(value)) {
            newCount += map.get(value);
        }

        map.put(value, newCount);
    }

}
