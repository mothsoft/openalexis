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
package com.mothsoft.alexis.rest.analysis.v1.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.rest.analysis.v1.AnalysisResource;
import com.mothsoft.alexis.rest.analysis.v1.Edge;
import com.mothsoft.alexis.rest.analysis.v1.Graph;
import com.mothsoft.alexis.rest.analysis.v1.Node;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DocumentService;

@Transactional
public class AnalysisResourceImpl implements AnalysisResource {

    private DocumentService documentService;

    public AnalysisResourceImpl(final DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public Graph getRelatedTerms(String query, int count) {
        final Long userId = CurrentUserUtil.getCurrentUserId();
        final com.mothsoft.alexis.domain.Graph domain = this.documentService.getRelatedTerms(query, userId, count);

        final List<Node> nodes = new ArrayList<Node>(domain.getNodes().size());
        for (final com.mothsoft.alexis.domain.Node ith : domain.getNodes()) {
            final Node node = new Node();
            node.setName(ith.getName());
            node.setRoot(ith.isRoot());
            nodes.add(node);
        }

        final List<Edge> edges = new ArrayList<Edge>(domain.getEdges().size());
        for (final com.mothsoft.alexis.domain.Edge ith : domain.getEdges()) {
            final Edge edge = new Edge();
            edge.setNodeA(ith.getNodeA().getName());
            edge.setNodeB(ith.getNodeB().getName());
            edges.add(edge);
        }

        final Graph graph = new Graph();
        graph.setNodes(nodes);
        graph.setEdges(edges);
        return graph;
    }

}
