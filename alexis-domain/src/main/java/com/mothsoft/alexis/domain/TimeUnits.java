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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public enum TimeUnits {
    MONTH, DAY, HOUR, MINUTE;

    public long getDuration() {
        switch (this.ordinal()) {
            case 0:
                return DateConstants.ONE_MONTH_IN_MILLISECONDS;
            case 1:
                return DateConstants.ONE_DAY_IN_MILLISECONDS;
            case 2:
                return DateConstants.ONE_HOUR_IN_MILLISECONDS;
            case 3:
                return DateConstants.ONE_MINUTE_IN_MILLISECONDS;
            default:
                throw new IllegalArgumentException("Unexpected ordinal value: " + this.ordinal());
        }
    }

    /**
     * For a given date, find the previous value that is an exact value in the
     * value set of timeUnits
     */
    public static Date floor(final Date date, final TimeUnits timeUnits) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        // fall through to get the flooring of each smaller unit
        switch (timeUnits) {
            case MONTH:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            case DAY:
                calendar.set(Calendar.HOUR_OF_DAY, 0);
            case HOUR:
                calendar.set(Calendar.MINUTE, 0);
            case MINUTE:
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            default:
                throw new IllegalArgumentException("Unexpected enum value: " + timeUnits.name());
        }

        return calendar.getTime();
    }

    /**
     * For a given date, find the next value that is an exact value in the value
     * set of timeUnits
     */
    public static Date ceil(final Date date, final TimeUnits timeUnits) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        // fall through to get the flooring of each smaller unit
        switch (timeUnits) {
            case MONTH:
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            case DAY:
                calendar.set(Calendar.HOUR_OF_DAY, 23);
            case HOUR:
                calendar.set(Calendar.MINUTE, 59);
            case MINUTE:
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                break;
            default:
                throw new IllegalArgumentException("Unexpected enum value: " + timeUnits.name());
        }

        return calendar.getTime();
    }

}