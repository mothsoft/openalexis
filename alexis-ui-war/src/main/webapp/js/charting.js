$(document).ready(function() {
	$("#startDate").datetimepicker({
		ampm : true,
		dateFormat : "mm/dd/yy",
		timeFormat : "h:mm TT"
	});
	$("#endDate").datetimepicker({
		ampm : true,
		dateFormat : "mm/dd/yy",
		timeFormat : "h:mm TT"
	});
});

var colors = new Array("#385d8a", "#8c3836", "#71893f", "#5c4776", "#357d91",
		"#b66d31", "#426da1", "#a44340", "#849f4b", "#6c548a", "#3f92a9",
		"#d37f3a", "#4b7bb4", "#b74c49", "#94b255", "#7a5f9a", "#47a4bd",
		"#ec8f42", "#7394c5", "#c87372", "#a9c379", "#9480ae", "#70b7cd",
		"#f8a56e", "#a1b4d4", "#d6a1a0", "#c0d2a4", "#b3a8c4", "#a0cad9",
		"#f9be9e");

function switchSeries() {
	$("#tooltip").remove();
	$(".waiting").show();
	var startDate = new Date($("#startDate").val());
	var endDate = new Date($("#endDate").val());
	var seriesIds = $("#selectSeries").val();

	if(!seriesIds) {
		return;
	}
	
	startDate = formatDate(startDate);
	endDate = formatDate(endDate);

	var seriesArray = new Array();
	var ajaxes = new Array();
	var i = -1;
	
	var selectedSeriesNames = new Array();
	
	$.each(seriesIds, function(index, seriesId) {
		if ($.isNumeric(seriesId) == false) {
			return;
		}

		i++;
		var j = i;
		var seriesName = $.trim($("#selectSeries option[value='" + seriesId + "']")
				.text());
		selectedSeriesNames.push(seriesName);
		
		var timestamp = new Date().getTime();
		ajaxes.push($.ajax({
			url : "/api/data-sets/v1/" + seriesId + "/points.json?startDate="
					+ startDate + "&endDate=" + endDate + "&ts=" + timestamp,
			beforeSend : function(xhr) {
				xhr.setRequestHeader("Authorization", authorization);
			},
			error : function(jqXhr, textStatus, errorThrown) {
				alert("Error " + errorThrown + ":" + textStatus);
			},
			success : function(val) {
				seriesArray[j] = {
					seriesId : seriesId,
					seriesName : seriesName,
					data : val
				};
			}
		}));
	});

	// seriesName => axis number (or null)
	var axesMap = buildAxesMap(selectedSeriesNames);

	$.when.apply(null, ajaxes).then(function(args) {
		plotAll(seriesArray, axesMap);
		$(".waiting").hide();
	});
}

function buildAxesMap(selectedSeriesNames) {
	var axesMap = new Object();

	var dataSetsByName = new Object();
	var timestamp = new Date().getTime();
	$.ajax({
		async: false,
		url : "/api/data-sets/v1.json?ts=" + timestamp,
		beforeSend : function(xhr) {
			xhr.setRequestHeader("Authorization", authorization);
		},
		error : function(jqXhr, textStatus, errorThrown) {
			alert("Error " + errorThrown + ":" + textStatus);
		},
		success : function(jsonData) {
			$.each(jsonData, function(key, value) {
			    var name = value["name"];
			    dataSetsByName[name] = value;
			});
		}
	});

	// a series is assigned an axis based on the following rules:
	// parent has axis: inherits parent
	// data set of type 'Stock Quotes': new axis
	// default: shared axis (1)
	var lastAxis = 1;

	// no parents first
	$.each(selectedSeriesNames, function(key, value) {
		var name = selectedSeriesNames[key];
		var value = dataSetsByName[name];
        var parentDataSetName = value["parentDataSetName"];
        if(! parentDataSetName) {
        	var type = value["type"];
    		var name = value["name"];
        	if(type == 'Stock Quotes') {
        		axesMap[name] = ++lastAxis;
        	} else {
        		axesMap[name] = 1;
        	}
        }
	});

	// parents: either inherit or shared
	$.each(selectedSeriesNames, function(key, value) {
		var name = selectedSeriesNames[key];
		var value = dataSetsByName[name];
        var parentDataSetName = value["parentDataSetName"];
        if(parentDataSetName) {
        	var name = value["name"]; 
        	var parentAxis = axesMap[parentDataSetName];
        	
        	if(parentAxis) {
        		axesMap[name] = parentAxis;
        	} else {
        		axesMap[name] = 1;
        	}
        }
	});
	
	return axesMap;
}

function plotAll(seriesArray, axesMap) {
	var yaxes = new Array();
	var length = 0;
	for(key in axesMap) {
	    length++;
	}
	yaxes.length = length;
	
	var options = {
		lines : {
			show : true
		},
		points : {
			show : true
		},
		xaxis : {
			mode : "time",
			twelveHourClock : true
		},
		yaxis : {
			labelWidth: 0,
			labelHeight: 0,
			position: "right",
			tickLength: 0,
			ticks: 0,
			tickSize: 0
		},
		xaxes: [],
		yaxes : yaxes,
		grid : {
			hoverable : true,
			clickable : false,
			mouseActiveRadius : 10
		},
		interaction : {
			redrawOverlayInterval: 60
		},
		legend : {
			show : true,
			container : "#flotLegend"
		}
	};
	var data = [];
	var placeholder = $("#flotChart");
	
	for (var i = 0; i < seriesArray.length; i++) {
		var jsonData = seriesArray[i].data;
		var seriesName = seriesArray[i].seriesName;
		var points = [];
		$.each(jsonData, function(key, value) {
			var x = value["x"];
			var y = value["y"];
			points.push([ x, y ]);
		});

		data.push({
			label : seriesName,
			data : points,
			color : colors[i % seriesArray.length],
			yaxis : axesMap[seriesName] ? axesMap[seriesName] : 1
		});
	}

	var plot = $.plot(placeholder, data, options);

	var last = [];
	
	$("#flotChart").bind("plothover", function(event, pos, item) {
		if(!item) {
			if(last[0] && last[1]) {
			    plot.unhighlight(last[0], last[1]);
			}
			last = [];
			$("#tooltip").remove();
		}
		else if (item) {
			var x = item.datapoint[0].toFixed(2), y = item.datapoint[1]
					.toFixed(2);
			if (last[0] != item.series && last[1] != item.datapoint) {
				last[0] = item.series;
				last[1] = item.datapoint;
				var theDate = new Date(item.datapoint[0].toFixed(0) / 1);
				var text = item.series.label + "<br />&nbsp;&nbsp;&nbsp;" + theDate.toLocaleDateString() + "<br />&nbsp;&nbsp;&nbsp;" + theDate.toLocaleTimeString() + "<br />&nbsp;&nbsp;&nbsp;" + y;
				showTooltip(item.pageX, item.pageY, text, item.series.color);
			} else {
				//toggle functionality
				last[0] = null;
				last[1] = null;
			}
		}
	});
}

function showTooltip(x, y, contents, color) {
	$("#tooltip").remove();
    $('<div id="tooltip">' + contents + '</div>').css( {
    	position: 'absolute',
    	display: 'none',
        left: x + 6,
        top: y,
        border: '1px solid #fdd',
        'background-color': color,
        'color': '#fff',
        opacity: 0.90
    }).appendTo("body").show();
}
