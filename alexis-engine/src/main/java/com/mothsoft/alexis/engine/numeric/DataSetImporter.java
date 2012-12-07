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
package com.mothsoft.alexis.engine.numeric;

/**
 * An interface for classes which know how to interface with systems or data
 * stores (internal or external) to import entire data sets or data set points.
 * In the case of internal systems, the expected use case is of capturing
 * aggregate or individual data points for a set time range. This allows
 * trending data sets in a standard way.
 * 
 */
public interface DataSetImporter {

    public void importData();

}
