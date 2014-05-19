package com.mothsoft.alexis.engine.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeUtil {

    /**
     * Find the last quarter-hour before date
     */
    public static Date floor(final Date date) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        int minute = calendar.get(Calendar.MINUTE);

        if (minute >= 45) {
            calendar.set(Calendar.MINUTE, 45);
        } else if (minute >= 30) {
            calendar.set(Calendar.MINUTE, 30);
        } else if (minute >= 15) {
            calendar.set(Calendar.MINUTE, 15);
        } else if (minute >= 0) {
            calendar.set(Calendar.MINUTE, 0);
        }

        return calendar.getTime();
    }

    /**
     * Add some time to a date
     */
    public static Date add(final Date date, final int calendarField, final int amount) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendarField, amount);
        return calendar.getTime();
    }

}
