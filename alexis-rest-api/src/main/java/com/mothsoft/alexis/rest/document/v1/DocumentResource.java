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
package com.mothsoft.alexis.rest.document.v1;

import java.sql.Timestamp;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/documents/v1/")
@Produces({ "application/json", "application/xml" })
public interface DocumentResource {

    @Path("/{id}")
    @GET
    public Document getDocument(@PathParam("id") String id);

    @Path("/{a}/similarity/{b}")
    @GET
    public Double getSimilarity(@PathParam("a") String aId, @PathParam("b") String bId);

    @Path("/{id}/text")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDocumentText(@PathParam("id") String id);

    @GET
    @Path("/important-terms")
    public ImportantTerms getImportantTerms(
            @QueryParam("startDate") @DefaultValue("1970-01-01 00:00:00") Timestamp startDate,
            @QueryParam("endDate") @DefaultValue("2100-12-31 23:59:59") Timestamp endDate,
            @QueryParam("count") @DefaultValue("20") int count,
            @QueryParam("filterStopWords") @DefaultValue("true") boolean filterStopWords);

}
