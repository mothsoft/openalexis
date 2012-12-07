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
package com.mothsoft.alexis.rest.analysis.v1;

public class Edge {

    private String nodeA;
    private String nodeB;

    public Edge() {
        super();
    }

    public String getNodeA() {
        return nodeA;
    }

    public void setNodeA(String nodeA) {
        this.nodeA = nodeA;
    }

    public String getNodeB() {
        return nodeB;
    }

    public void setNodeB(String nodeB) {
        this.nodeB = nodeB;
    }

}
