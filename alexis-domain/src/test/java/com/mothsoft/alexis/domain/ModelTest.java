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

import static org.junit.Assert.*;

import org.junit.Test;

public class ModelTest {

    /**
     * Make sure the outcomes are correctly ordered
     */
    @Test
    public void testOutcomeArray() {
        double last = -1.0d;
        for (int i = 0; i < Model.OUTCOME_ARRAY.length; i++) {
            assertTrue("outcome " + Model.OUTCOME_ARRAY[i] + " is out of order", Model.OUTCOME_ARRAY[i] > last);
            last = Model.OUTCOME_ARRAY[i];
        }
    }

}
