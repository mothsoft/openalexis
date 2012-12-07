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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import opennlp.maxent.GIS;
import opennlp.model.Event;
import opennlp.model.EventStream;
import opennlp.model.ListEventStream;
import opennlp.model.MaxentModel;

import org.junit.Before;
import org.junit.Test;

public class OpenNLPMaxentTest {

    MaxentModel model;

    @Before
    public void setUp() throws IOException {
        final List<Event> events = new ArrayList<Event>(4);
        final Event boy1 = new Event("boy", new String[] { "tall", "short_hair", "muscular" });
        final Event boy2 = new Event("boy", new String[] { "tall", "long_hair", "muscular" });
        final Event girl1 = new Event("girl", new String[] { "short", "long_hair", "slim" });
        final Event girl2 = new Event("girl", new String[] { "short", "long_hair", "slim" });

        events.add(boy1);
        events.add(boy2);
        events.add(girl1);
        events.add(girl2);

        final EventStream eventStream = new ListEventStream(events);

        this.model = GIS.trainModel(eventStream);
    }

    @Test
    public void test() {
        final double[] shortOutcomes = this.model.eval(new String[] { "short" });
        final String bestOutcomeShort = this.model.getBestOutcome(shortOutcomes);
        assertEquals("girl", bestOutcomeShort);

        final double[] tallOutcomes = this.model.eval(new String[] { "tall" });
        final String bestOutcomeTall = this.model.getBestOutcome(tallOutcomes);
        assertEquals("boy", bestOutcomeTall);

        final double[] shortShortOutcomes = this.model.eval(new String[] { "short", "short_hair", "muscular" });
        assertEquals("boy", this.model.getBestOutcome(shortShortOutcomes));
    }

}
