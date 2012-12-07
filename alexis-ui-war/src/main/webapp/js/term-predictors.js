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

function switchSeries(authorization) {
	$("#chartPointInteraction").hide();
	var startDate = new Date($("#startDate").val());
	var endDate = new Date($("#endDate").val());
	var seriesId = $("#selectSeries").val();

	if ($.isNumeric(seriesId) == false) {
		return;
	}

	startDate = formatDate(startDate);
	endDate = formatDate(endDate);

	$.ajax({
		url : "/api/data-sets/v1/" + seriesId + "/points.json?startDate="
				+ startDate + "&endDate=" + endDate,
		beforeSend : function(xhr) {
			xhr.setRequestHeader("Authorization", authorization);
		},
		error : function(jqXhr, textStatus, errorThrown) {
			alert("Error " + errorThrown + ":" + textStatus);
		},
		success : function(val) {
			plotSeries(val);
		}
	});
}

function plotSeries(jsonData) {
	var points = [];
	$.each(jsonData, function(key, value) {
		var x = value["x"];
		var y = value["y"];
		points.push([ x, y ]);
	});

	var plot = $.plot($("#flotChart"), [ {
		data : points,
		label : ""
	} ], {
		series : {
			color : "#385D8A",
			lines : {
				show : true
			},
			points : {
				show : true
			}
		},
		legend : {
			show : false
		},
		grid : {
			hoverable : true,
			clickable : true,
			autoHighlight : false
		},
		xaxis : {
			mode : "time"
		},
		yaxis : {
		}
	});

	$("#flotChart").bind("plothover", function(event, pos, item) {
		// TODO
	});

	var last = [];
	$("#flotChart").bind(
			"plotclick",
			function(event, pos, item) {
				if (item) {
					$(".resultsContainer").hide();
					var x = item.datapoint[0].toFixed(2), y = item.datapoint[1]
							.toFixed(2);
					$("#x").text(new Date(parseInt(x)).toLocaleString());
					$("#pointX").val(parseInt(x));
					$("#pointY").val(y);
					$("#y").text(y);
					if (last[0] && last[1]) {
						plot.unhighlight(last[0], last[1]);
					}
					plot.highlight(item.series, item.datapoint);
					last[0] = item.series;
					last[1] = item.datapoint;
					$("#chartPointInteraction").show();
					$(".resultsContainer").hide();
				}
			});
}