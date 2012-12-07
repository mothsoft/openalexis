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

public class ChartingBackingBean {

    private SelectSeriesBackingBean selectSeriesBackingBean;

    public ChartingBackingBean() {
    }

    public SelectSeriesBackingBean getSelectSeriesBackingBean() {
        return selectSeriesBackingBean;
    }

    public void setSelectSeriesBackingBean(SelectSeriesBackingBean selectSeriesBackingBean) {
        this.selectSeriesBackingBean = selectSeriesBackingBean;
    }

}
