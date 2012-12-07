/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Author: Kyle Scholz      http://kylescholz.com/
 * Copyright: 2006-2007
 */

/**
 * Seed DataGraph with contents of a JSON tree structure.
 * 
 * @author Kyle Scholz
 * 
 * @version 0.3
 */
var JSONLoader = function(dataGraph) {
	this.subscribers = new Array();
	this.dataGraph = dataGraph;
	this.http = new HTTP();
}

/*
 * @param {Object} subscriber
 */
JSONLoader.prototype.subscribe = function(subscriber) {
	this.subscribers.push(subscriber);
}

/*
 * 
 */
JSONLoader.prototype.notify = function() {
	for ( var i = 0; i < this.subscribers.length; i++) {
		this.subscribers[i].notify();
	}
}

/*
 * Fetch XML data for processing
 */
JSONLoader.prototype.load = function(url) {
	this.http.get(url, this, this.handle);
}

/*
 * Process XML data in DataGraph.
 * 
 */
JSONLoader.prototype.handle = function(response) {
	var json = response.responseText;
	var data = $.parseJSON(json);
	var dataGraph = this.dataGraph;

	var rootNode;
	var nodes = Object();

	$.each(data.nodes, function(index, element) {
		var node = new DataGraphNode();
		node.label = element.name;
		node.color = "#fff";
		nodes[element.name] = node;

		if (index == 0) {
			node.root = true;
			node.fixed = true;
			rootNode = node;
		}
		dataGraph.addNode(node);
	});

	if (data.edges) {
		$.each(data.edges, function(index, element) {
			var nodeA = nodes[element.nodeA];
			var nodeB = nodes[element.nodeB];

			if (nodeA != nodeB && nodeA == rootNode) {
				nodeB.parent = nodeA;
			}
		});
	}
	/*
	 * for ( var key in nodes) { dataGraph.addNode(nodes[key]); }
	 */
	if (data.edges) {
		$.each(data.edges, function(index, element) {
			var nodeA = nodes[element.nodeA];
			var nodeB = nodes[element.nodeB];

			if (nodeA != rootNode) {
				dataGraph.addEdge(nodeA, nodeB);
			}
		});
	}

	this.notify();
}
