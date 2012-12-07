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
package com.mothsoft.integration.twitter;

import java.util.List;

import twitter4j.SavedSearch;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public interface TwitterService {

    public User login(String oauthToken, String oauthTokenSecret);

    public RequestToken getRequestToken();

    public AccessToken getAccessToken(RequestToken requestToken, String verificationCode);

    public void createSavedSearch(String query);

    public List<SavedSearch> listSavedSearches();

    public List<Tweet> search(String query);

    /**
     * Get the home timeline corresponding to the credentials' user, greater
     * than the specified since ID, going back a maximum of maximumNumber of
     * records. Newest statuses are sorted to the front.
     * 
     * @param accessToken
     * @param sinceId
     * @param maximumNumber
     * @return
     */
    public List<Status> getHomeTimeline(AccessToken accessToken, Long sinceId, Short maximumNumber);

}
