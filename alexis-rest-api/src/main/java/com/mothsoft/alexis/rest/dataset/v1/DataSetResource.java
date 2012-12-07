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
package com.mothsoft.alexis.rest.dataset.v1;

import java.sql.Timestamp;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/data-sets/v1/")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface DataSetResource {

    @GET
    @Path("/{id}")
    public DataSet getDataSet(@PathParam("id") Long id);

    @GET
    @Path("/{id}/points")
    public List<DataSetPoint> findAndAggregatePointsGroupedByUnit(@PathParam("id") Long dataSetId,
            @QueryParam("startDate") @DefaultValue("1970-01-01 00:00:00") Timestamp startDate,
            @QueryParam("endDate") @DefaultValue("2100-12-31 23:59:59") Timestamp endDate,
            @QueryParam("units") String units);

    @GET
    public List<DataSet> list();

    @GET
    @Path("/correlation/{a}/{b}/{units}")
    public Correlation correlate(@PathParam("a") Long dataSetAId, @PathParam("b") Long dataSetBId,
            @QueryParam("startDate") Timestamp startDate, @QueryParam("endDate") Timestamp endDate,
            @PathParam("units") String units);

}
