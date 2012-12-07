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

import java.io.IOException;
import java.net.URLEncoder;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

public class SearchBackingBean {

    private String searchExpression;

    public String getSearchExpression() {
        return this.searchExpression;
    }

    public void setSearchExpression(String searchExpression) {
        this.searchExpression = searchExpression;
    }

    public void search(final ActionEvent event) throws IOException {
        final FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().redirect(
                "/" + context.getExternalContext().getContextName() + "/documents/search?q="
                        + URLEncoder.encode(this.searchExpression, "UTF-8"));
    }

}
