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

public interface DateConstants {

    public static final Long ONE_MINUTE_IN_MILLISECONDS = 60L * 1000L;

    public static final Long ONE_DAY_IN_MILLISECONDS = 86400000L;

    public static final Long ONE_HOUR_IN_MILLISECONDS = (long) (ONE_DAY_IN_MILLISECONDS / 24);

    public static final Long ONE_WEEK_IN_MILLISECONDS = (long) (7 * ONE_DAY_IN_MILLISECONDS);

    public static final Long ONE_MONTH_IN_MILLISECONDS = 30 * ONE_DAY_IN_MILLISECONDS;

    public static final Long THREE_DAYS_IN_MILLISECONDS = (long) (3 * ONE_DAY_IN_MILLISECONDS);

    public static final Long TWELVE_HOURS_IN_MILLISECONDS = ONE_DAY_IN_MILLISECONDS / 2;

}
