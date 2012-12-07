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
package com.mothsoft.alexis.engine.textual;

import java.util.List;

import org.apache.log4j.Logger;

import com.mothsoft.alexis.engine.Task;

public class CompositeTaskImpl implements Task {

    private static final Logger logger = Logger.getLogger(TransactionalCompositeTaskImpl.class);

    private final List<Task> tasks;

    public CompositeTaskImpl(final List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void execute() {
        for (final Task task : this.tasks) {
            try {
                task.execute();
            } catch (final Exception e) {
                logger.error("Task: " + task.getClass().getName() + " failed with: " + e, e);
            }
        }
    }

}
