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
package com.mothsoft.alexis.web;

import java.util.List;

import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Model;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.ModelService;

public class ListModelsBackingBean {

    private DataRange<Model> models = null;
    private ModelService modelService;

    public ListModelsBackingBean() {
        super();
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public List<Model> getModels() {
        if (this.models == null) {
            this.models = this.modelService.listByOwner(CurrentUserUtil.getCurrentUserId(), 0, Integer.MAX_VALUE);
        }

        return this.models.getRange();
    }

}
