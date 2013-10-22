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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.hibernate.QueryException;

import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentScore;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.TopicDocument;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DocumentService;

public class ListDocumentsBackingBean {

    public enum Filter {
        TOPICS, ALL, SEARCH
    }

    private static final int PAGE_SIZE = 5;

    private DocumentService documentService;

    private DataRange<Document> dataRange;
    private Map<String, List<TopicDocument>> topicDocuments;
    private int pageNumber = 0;
    private Filter filter;
    private boolean queryValidationError;

    public ListDocumentsBackingBean() {
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public List<Document> getDocuments() {
        init();
        return this.dataRange.getRange();
    }

    public Map<String, List<TopicDocument>> getTopicDocuments() {
        init();
        return this.topicDocuments;
    }

    public Integer getNewerPageNumber() {
        init();

        if (this.pageNumber <= 0) {
            return 0;
        }

        return (this.pageNumber - 1);
    }

    public Integer getOlderPageNumber() {
        init();

        final int totalPages = (this.dataRange.getTotalRowsAvailable() / PAGE_SIZE)
                + (this.dataRange.getTotalRowsAvailable() % PAGE_SIZE == 0 ? 0 : 1);
        final int lastPageNumber = totalPages - 1;

        if (this.pageNumber >= lastPageNumber) {
            return this.pageNumber;
        }
        return (this.pageNumber + 1);
    }

    public Integer getPageNumber() {
        init();
        return this.pageNumber;
    }

    public boolean isAll() {
        return Filter.ALL.equals(readFilter());
    }

    public boolean isTopics() {
        return Filter.TOPICS.equals(readFilter());
    }

    public boolean isSearch() {
        return Filter.SEARCH.equals(readFilter());
    }

    private Filter readFilter() {
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        final String paramValue = facesContext.getExternalContext().getRequestParameterMap().get("type");

        if (paramValue == null) {
            return Filter.TOPICS;
        } else {
            return Filter.valueOf(paramValue.toUpperCase());
        }
    }

    private Integer readPage() {
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        final String paramValue = facesContext.getExternalContext().getRequestParameterMap().get("page");

        if (paramValue == null) {
            return Integer.valueOf(0);
        } else {
            return Integer.valueOf(paramValue);
        }
    }

    public String getSearchString() {
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        final String paramValue = facesContext.getExternalContext().getRequestParameterMap().get("q");
        return paramValue;
    }

    public SortOrder getSortOrder() {
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        final String paramValue = facesContext.getExternalContext().getRequestParameterMap().get("order");

        if (StringUtils.trimToEmpty(paramValue).equalsIgnoreCase("date")) {
            return SortOrder.DATE_DESC;
        } else {
            return SortOrder.RELEVANCE;
        }
    }

    public String getSortOrderString() {
        final SortOrder sortOrder = getSortOrder();
        switch (sortOrder) {
            case DATE_DESC:
                return "date";
            default:
                return "";
        }
    }

    public String getSearchStringEncoded() {
        String result = "";
        try {
            result = URLEncoder.encode(getSearchString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // won't happen - UTF-8 is standard
        }
        return result;
    }

    public Integer getTotalRowsAvailable() {
        init();
        return this.dataRange.getTotalRowsAvailable();
    }

    public boolean isQueryValidationError() {
        init();
        return this.queryValidationError;
    }

    @SuppressWarnings("unchecked")
    private void init() {
        synchronized (this) {
            if (this.dataRange == null) {
                this.pageNumber = readPage();
                this.filter = readFilter();

                final int start = this.pageNumber * PAGE_SIZE;

                final Long userId = CurrentUserUtil.getCurrentUserId();

                if (Filter.ALL.equals(filter)) {
                    this.dataRange = this.documentService.listDocumentsByOwner(userId, start, PAGE_SIZE);
                } else if (Filter.SEARCH.equals(filter)) {
                    try {
                        final String query = getSearchString();
                        this.dataRange = toDocumentList(this.documentService.searchByOwnerAndExpression(userId, query,
                                getSortOrder(), start, PAGE_SIZE));
                    } catch (final QueryException queryException) {
                        this.dataRange = new DataRange<Document>(Collections.EMPTY_LIST, 0, 0);
                        this.queryValidationError = true;
                        return;
                    }
                } else {
                    this.dataRange = this.documentService.listDocumentsInTopicsByOwner(userId, start, PAGE_SIZE);
                }

                this.topicDocuments = new HashMap<String, List<TopicDocument>>();

                for (final Document ith : this.dataRange.getRange()) {
                    this.topicDocuments.put(ith.getId(), this.documentService.getTopicDocuments(ith.getId()));
                }
            }
        }

    }

    // FIXME - collapsing to same data types as other documents, but will
    // probably want to leverage the scores in the UI beyond sorting at some
    // point
    private DataRange<Document> toDocumentList(DataRange<DocumentScore> scoredRange) {
        final List<Document> range = new ArrayList<Document>(scoredRange.getRange().size());

        for (final DocumentScore scoredDoc : scoredRange.getRange()) {
            range.add(scoredDoc.getDocument());
        }

        final DataRange<Document> dataRange = new DataRange<Document>(range, scoredRange.getFirstRow(),
                scoredRange.getTotalRowsAvailable());
        return dataRange;
    }
}
