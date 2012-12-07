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

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.component.UIInput;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.log4j.Logger;

import com.mothsoft.alexis.domain.DateConstants;
import com.mothsoft.alexis.domain.ImportantNamedEntity;
import com.mothsoft.alexis.domain.ImportantTerm;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DocumentService;

public class TermPredictorsBackingBean {
    private static final Logger logger = Logger.getLogger(TermPredictorsBackingBean.class);

    private SelectSeriesBackingBean selectSeriesBackingBean;
    private DocumentService documentService;

    // parameters
    private String explain;
    private Long pointX;
    private Double pointY;
    private Long windowSize; // in milliseconds

    // results
    private List<ImportantTerm> topTerms;
    private int topTermsMaxCount;

    private List<ImportantNamedEntity> topNames;
    private int topNamesMaxCount;

    public TermPredictorsBackingBean() {
        super();
    }

    public void setSelectSeriesBackingBean(SelectSeriesBackingBean selectSeriesBackingBean) {
        this.selectSeriesBackingBean = selectSeriesBackingBean;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void changeExplain(final AjaxBehaviorEvent event) {
        final UIInput input = (UIInput) event.getComponent();
        final String value = (String) input.getValue();
        this.explain = value;

        if ("point".equals(this.explain)) {
            this.windowSize = null;
        }
    }

    public void analyze(final AjaxBehaviorEvent event) {
        this.topNames = null;
        this.topTerms = null;

        // recalculate
        final Date graphStart = this.selectSeriesBackingBean.getStartDate();
        final Date graphEnd = this.selectSeriesBackingBean.getEndDate();

        Date rangeStart = null;
        Date rangeEnd = null;

        if ("point".equals(this.explain)) {
            final Long graphDuration = graphEnd.getTime() - graphStart.getTime();
            this.windowSize = (graphDuration > 14 * DateConstants.ONE_DAY_IN_MILLISECONDS) ? DateConstants.ONE_DAY_IN_MILLISECONDS
                    : DateConstants.ONE_HOUR_IN_MILLISECONDS;
            rangeStart = new Date(this.pointX);
            rangeEnd = new Date(this.pointX + this.windowSize);
        } else if ("leading".equals(this.explain)) {
            rangeStart = new Date(this.pointX - this.windowSize);
            rangeEnd = new Date(this.pointX);
        } else if ("lagging".equals(this.explain)) {
            rangeStart = new Date(this.pointX);
            rangeEnd = new Date(this.pointX + this.windowSize);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for term predictors of type: " + this.explain + " over range: " + rangeStart
                    + " to " + rangeEnd);
        }

        final Long userId = CurrentUserUtil.getCurrentUserId();

        // important names
        this.topNames = this.documentService.getImportantNamedEntities(userId, rangeStart, rangeEnd, 25);

        this.topNamesMaxCount = 1;
        for (final ImportantNamedEntity entity : this.topNames) {
            if (entity.getCount() > this.topNamesMaxCount) {
                this.topNamesMaxCount = entity.getCount();
            }
        }
        Collections.sort(this.topNames, ImportantNamedEntity.NAME_COMPARATOR);

        // important terms
        this.topTerms = this.documentService.getImportantTerms(userId, new Timestamp(rangeStart.getTime()),
                new Timestamp(rangeEnd.getTime()), 25, true);

        this.topTermsMaxCount = 1;
        for (final ImportantTerm term : this.topTerms) {
            if (term.getCount() > this.topTermsMaxCount) {
                this.topTermsMaxCount = term.getCount();
            }
        }
        Collections.sort(this.topTerms, ImportantTerm.NAME_COMPARATOR);
    }

    public boolean isSeriesSelected() {
        return this.selectSeriesBackingBean.getSelectedSeries() != null
                && !this.selectSeriesBackingBean.getSelectedSeries().isEmpty();
    }

    public boolean isShowWindowSize() {
        return "leading".equals(this.explain) || "lagging".equals(this.explain);
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public Long getPointX() {
        return pointX;
    }

    public void setPointX(Long pointX) {
        this.pointX = pointX;
    }

    public Double getPointY() {
        return pointY;
    }

    public void setPointY(Double pointY) {
        this.pointY = pointY;
    }

    public Long getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(Long windowSize) {
        this.windowSize = windowSize;
    }

    public List<ImportantTerm> getTopTerms() {
        return topTerms;
    }

    public int getTopTermsMaxCount() {
        return topTermsMaxCount;
    }

    public void setTopTerms(List<ImportantTerm> topTerms) {
        this.topTerms = topTerms;
    }

    public List<ImportantNamedEntity> getTopNames() {
        return topNames;
    }

    public void setTopNames(List<ImportantNamedEntity> topNames) {
        this.topNames = topNames;
    }

    public int getTopNamesMaxCount() {
        return topNamesMaxCount;
    }

}
