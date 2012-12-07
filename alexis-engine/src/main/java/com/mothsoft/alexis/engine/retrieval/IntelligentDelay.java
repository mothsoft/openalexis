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
package com.mothsoft.alexis.engine.retrieval;

import org.apache.log4j.Logger;

/**
 * Works very nicely--but don't use this inside a transaction!
 * 
 * @author tgarrett
 * 
 */
public class IntelligentDelay {

    private static final Logger logger = Logger.getLogger(IntelligentDelay.class);

    private String taskName;
    private Integer growBySeconds;
    private Integer upToMaxSeconds;

    private Integer counter = 0;

    public IntelligentDelay(final String taskName, final Integer growBySeconds, final Integer upToMaxSeconds) {
        this.taskName = taskName;
        this.growBySeconds = growBySeconds;
        this.upToMaxSeconds = upToMaxSeconds;
    }

    /**
     * reset the counter so delay will be minimum time
     */
    public void reset() {
        this.counter = 0;
    }

    /**
     * Sleep for an amount of time dictated by number of times asked to sleep
     */
    public void sleep() {

        if (this.counter >= (this.upToMaxSeconds / this.growBySeconds)) {
            this.counter = 0;
        }
        this.counter++;

        long sleepTime = this.growBySeconds * counter * 1000;

        logger.info("Sleeping task '" + taskName + "' for: " + sleepTime + " milliseconds");

        try {
            Thread.sleep(sleepTime);
        } catch (final InterruptedException e) {
            return;
        }

    }

}
