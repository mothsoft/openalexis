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

/**
 * Support class for common calculations
 * 
 */
public class Calculator {

    /**
     * Calculate the percent change from a to b. In the event that transition is
     * (n->0) or (0->n), do some magic based on the series maximum.
     * 
     * @param theOld
     * @param theNew
     * @param max
     * @return
     */
    public static double calculatePercentChange(final double theOld, final double theNew, final double max) {
        double percentChange;

        if (theOld == 0.0d) {
            percentChange = theNew / max;
        } else if (theNew == 0.0d) {
            percentChange = -1 * (theOld / max);
        } else {
            final double delta = Math.abs(theNew - theOld);
            percentChange = delta / Math.abs(theOld);
            percentChange *= theOld > theNew ? -1 : 1;
        }

        return percentChange;
    }
}
