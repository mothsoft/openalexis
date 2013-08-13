var rootNode;
var selectedNodes = new Array();
var selectedNodesQueryString = "";
var i = 0;

if (!Array.indexOf) {
	Array.indexOf = [].indexOf ? function(arr, obj, from) {
		return arr.indexOf(obj, from);
	} : function(arr, obj, from) { // (for IE6)
		var l = arr.length, i = from ? parseInt(
				(1 * from) + (from < 0 ? l : 0), 10) : 0;
		i = i < 0 ? 0 : i;
		for (; i < l; i++) {
			if (i in arr && arr[i] === obj) {
				return i;
			}
		}
		return -1;
	};
}

function init() {
	$("#selectedNodes").html("");
	$("#nodeInfo").hide();
	$("#selectedNodes").hide();
	selectedNodes = new Array();
	selectedNodesQueryString = "";
	i = 0;
	
	try {
		$('#queryModal').dialog('close');
	} catch(e) {
		//ignore for now
	}

	var queryString = $("#query").val();
	$("#canvas").html("");
	
	showWaitingModal();
	buildRelatedTermsGraph(
			"/api/analysis/v1/related-terms.json",
			queryString, 48);
}

function showModal() {
	$("#queryModal").dialog({
		draggable : false,
		modal : true,
		resizable : false,
		title : "Start"
	});
	$("#queryModal").dialog("open");
	$("#query").focus();
	$("#query").select();
}

function showWaitingModal() {
	$("#waitingModal").dialog({
		draggable : false,
		modal : true,
		resizable : false,
		title : "Loading..."
	});
	$("#waitingModal").dialog("open");
	$("#waitingModal img.spinner").show();
}

function hideWaitingModal() {
	$("#waitingModal").dialog("close");
	$("#waitingModal img.spinner").hide();
}

function startOver() {
	showModal();
}

function markSelected(node) {
	$("#nodeInfo").show();
	$("#selectedNodes").show();

	if (!selectedNodes || selectedNodes.length == 0) {
		selectedNodesQueryString = "";
	}

	var index = selectedNodes.indexOf(node);
	if (index > -1) {
		selectedNodes.splice(index, 1);
		i = selectedNodes.length;
		node.className = "node";

		if (node == rootNode) {
			node.className += " root";
		}
	} else {
		selectedNodes[i++] = node;
		node.style.color = '';
		node.style.backgroundColor = '';
		node.className = "node selectedNode";
		var term = node.innerText.charAt(0) == '+' ? node.innerText : "+"
				+ node.innerText;
		selectedNodesQueryString += (term + " ");
	}

	$("#selectedNodes").html("");

	var selectedNodesDiv = $("#selectedNodes");

	if (selectedNodes.length == 0) {
		selectedNodesDiv.hide();
	} else {
		for ( var j = 0; j < selectedNodes.length; j++) {
			selectedNodesDiv.append('<div class="node selectedNode">'
					+ selectedNodes[j].innerText + '</div>');
		}
	}
	$("#nodeInfo").show();
}

$(document).ready(function() {
	
    var queryString = $("#query").val();
    if (queryString) {
    	$('#queryModal').hide();
        init();
    } else {
        showModal();
    }

	$(".modalAdvanced .toggleDefaultHide").hide();
	$(".modalAdvanced .header").click(function() {
		$(this).next(".toggle").slideToggle(500);
		$("#startDate").val("");
		$("#endDate").val("");
	});

	$("#startDate").datetimepicker({
		ampm : true
	});
	$("#endDate").datetimepicker({
		ampm : true
	});
});

HTTP.prototype.handleTimeout = function() {
	if (this.pendingRequest && this.pendingRequest.abort) {
		this.pendingRequest.abort();
	}
	hideWaitingModal();
	alert("Request timed out. Please try again.");
};
