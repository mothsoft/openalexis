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

import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.engine.Task;

public class TransactionalCompositeTaskImpl extends CompositeTaskImpl implements Task {

    public TransactionalCompositeTaskImpl(final List<Task> tasks) {
        super(tasks);
    }

    @Transactional
    public void execute() {
        super.execute();
    }

}
