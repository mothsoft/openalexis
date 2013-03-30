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
package com.mothsoft.alexis.rest.source.v1.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.rest.source.v1.Source;
import com.mothsoft.alexis.rest.source.v1.SourceResource;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.SourceService;

@Transactional
public class SourceResourceImpl implements SourceResource {

    private SourceService service;

    public SourceResourceImpl(final SourceService service) {
        this.service = service;
    }

    @Override
    public List<Source> listRssSources() {
        final List<com.mothsoft.alexis.domain.Source> sources = this.service.listRssSourcesByOwner(CurrentUserUtil
                .getCurrentUserId());
        final List<Source> dtos = new ArrayList<Source>(sources.size());

        for (final com.mothsoft.alexis.domain.Source source : sources) {
            dtos.add(toDto((com.mothsoft.alexis.domain.RssSource) source));
        }

        return dtos;
    }

    private Source toDto(com.mothsoft.alexis.domain.RssSource domain) {
        final com.mothsoft.alexis.rest.source.v1.Source source = new com.mothsoft.alexis.rest.source.v1.Source();
        source.setId(domain.getId());
        source.setUrl(domain.getFeed().getUrl());
        return source;
    }
}
