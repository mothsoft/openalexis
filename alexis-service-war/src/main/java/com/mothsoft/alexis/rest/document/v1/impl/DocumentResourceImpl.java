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
package com.mothsoft.alexis.rest.document.v1.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.domain.DocumentType;
import com.mothsoft.alexis.domain.TweetFormatter;
import com.mothsoft.alexis.rest.document.v1.Document;
import com.mothsoft.alexis.rest.document.v1.DocumentResource;
import com.mothsoft.alexis.rest.document.v1.ImportantTerm;
import com.mothsoft.alexis.rest.document.v1.ImportantTerms;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DocumentService;

@Transactional
public class DocumentResourceImpl implements DocumentResource {

    private DocumentService service;

    public DocumentResourceImpl(final DocumentService service) {
        this.service = service;
    }

    @Override
    public Document getDocument(String id) {
        final com.mothsoft.alexis.domain.Document domain = this.service.getDocument(id);
        return toDto(domain);
    }

    @Override
    public Double getSimilarity(String aId, String bId) {
        return this.service.getSimilarity(aId, bId);
    }

    @Override
    public String getDocumentText(String id) {
        return this.service.getDocument(id).getContent();
    }

    @Override
    public ImportantTerms getImportantTerms(Timestamp startDate, Timestamp endDate, int count, boolean filterStopWords) {
        final Long userId = CurrentUserUtil.getCurrentUserId();
        final List<com.mothsoft.alexis.domain.ImportantTerm> terms = this.service.getImportantTerms(userId, startDate,
                endDate, count, filterStopWords);
        final List<ImportantTerm> dtos = new ArrayList<ImportantTerm>(terms.size());

        for (final com.mothsoft.alexis.domain.ImportantTerm term : terms) {
            dtos.add(new ImportantTerm(term.getTermValue(), term.getCount(), term.getTfIdf()));
        }

        return new ImportantTerms(dtos);
    }

    private Document toDto(com.mothsoft.alexis.domain.Document domain) {
        final com.mothsoft.alexis.rest.document.v1.Document document;

        if (domain.getType().equals(DocumentType.T)) {
            final com.mothsoft.alexis.domain.Tweet domainTweet = (com.mothsoft.alexis.domain.Tweet) domain;
            final com.mothsoft.alexis.rest.document.v1.Tweet tweet = new com.mothsoft.alexis.rest.document.v1.Tweet();
            document = tweet;

            tweet.setFormattedText(TweetFormatter.format(domainTweet));
            tweet.setFullName(domainTweet.getFullName());
            tweet.setProfileImageUrl(domainTweet.getProfileImageUrl());
            tweet.setTweetId(domainTweet.getTweetId());
            tweet.setRetweet(domainTweet.isRetweet());
            tweet.setRetweetUserName(domainTweet.getRetweetUserName());
            tweet.setScreenName(domainTweet.getScreenName());
        } else {
            document = new com.mothsoft.alexis.rest.document.v1.Document();
        }

        document.setId(domain.getId());
        document.setCreationDate(domain.getCreationDate());
        document.setDescription(domain.getDescription());
        document.setRetrievalDate(domain.getRetrievalDate());
        document.setState(domain.getState().name());
        document.setTermCount(domain.getTermCount());
        document.setTitle(domain.getTitle());
        document.setType(domain.getType().name());
        document.setUrl(domain.getUrl());

        return document;
    }
}
