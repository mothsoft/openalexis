function buildRelatedTermsGraph(url, queryString, count) {
	var layout = new ForceDirectedLayout(document.getElementById("canvas"),
			false);
	
	layout.config._default = {
		model : function(dataNode) {
			return {
				childRadius: 50,
				mass: 3,
			}
		},
		view : function(dataNode, modelNode) {
			hideWaitingModal();
			
			var nodeElement = document.createElement('div');
			nodeElement.style.position = "absolute";
			nodeElement.className = "node";
			nodeElement.innerHTML = dataNode.label;
			nodeElement.onmousedown = new EventHandler(layout,
					layout.handleMouseDownEvent, modelNode.id)
			
			nodeElement.onmouseover = new EventHandler(layout, function(event) {
				event.target.style.zIndex = 500;

				var index = selectedNodes.indexOf(event.target);
				if(index < 0) {
                    event.target.style.backgroundColor = 'beige';
			        event.target.style.color = '#000';
				}
			});

			nodeElement.onmouseout = new EventHandler(layout, function(event) {
				event.target.style.zIndex = 50;

				var index = selectedNodes.indexOf(event.target);
				if(index < 0) {
                    event.target.style.backgroundColor = '';
			        event.target.style.color = '';
				}
			});
			
			nodeElement.onclick = new EventHandler(layout, function(event) {
				event.target.style.zIndex = 499;
				markSelected(event.target);
			});

			dataNode.color.replace("#", "");

			if (dataNode.root) {
				nodeElement.className += (" " + "root");
				rootNode = nodeElement;
			}

			return nodeElement;
		}
	}

	layout.forces.spring._default = function(nodeA, nodeB, isParentChild) {
		if(isParentChild) {
			return {
				springConstant: 0.1,
				dampingConstant: 0.2,
				restLength: 180
			}
		} else {
			return {
				springConstant: 0.2,
				dampingConstant: 0.2,
				restLength: 85
			}
		}
	}

	layout.forces.magnet = function() {
		return {
			magnetConstant : -500,
			minimumDistance : 20
		}
	}

	/*
	 * 3) Override the default edge properties builder.
	 * 
	 * @return DOMElement
	 */
	layout.viewEdgeBuilder = function(dataNodeSrc, dataNodeDest) {
		if(dataNodeDest.parent && dataNodeDest.parent == dataNodeSrc) {
			return {
				'pixelColor' : "#999",
				'pixelWidth' : '2px',
				'pixelHeight' : '2px',
				'pixels' : 20
			}
		} else {
			return {
				'pixelColor' : "#000",
				'pixelWidth' : '2px',
				'pixelHeight' : '2px',
				'pixels' : 12
			}
		}
	}

	// layout.model.ENTROPY_THROTTLE = false;

	var loader = new JSONLoader(layout.dataGraph);
	var encodedUrl = url + "?q=" + encodeURIComponent(queryString) + "&count="
			+ count;
	loader.load(encodedUrl);

	var buildTimer = new Timer(50);
	buildTimer.subscribe(layout);
	buildTimer.start();
}
