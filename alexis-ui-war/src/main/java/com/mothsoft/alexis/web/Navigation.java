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

import javax.faces.context.FacesContext;

public class Navigation {

    private enum TopLevel {
        dashboard, topics, sources, documents, analysis
    }

    public Navigation() {
    }

    public final boolean isAnalysis() {
        return isPathMatch(TopLevel.analysis);
    }

    public final boolean isDashboard() {
        return isPathMatch(TopLevel.dashboard);
    }

    public final boolean isDocuments() {
        return isPathMatch(TopLevel.documents);
    }

    public final boolean isSources() {
        return isPathMatch(TopLevel.sources);
    }

    public final boolean isTopics() {
        return isPathMatch(TopLevel.topics);
    }

    private boolean isPathMatch(final TopLevel level) {
        return getRequestPath().startsWith("/" + level.name());
    }

    private String getRequestPath() {
        final String path = FacesContext.getCurrentInstance().getExternalContext().getRequestServletPath();
        return path;
    }
}
