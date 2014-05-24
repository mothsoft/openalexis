function validateRequired(object) {
	return object.val().trim().length > 0;
}

function formatDateUTC(dt) {
	return dt.getUTCFullYear() + "-" + pad2(1 + dt.getUTCMonth()) + "-"
			+ pad2(dt.getUTCDate()) + "+" + pad2(dt.getUTCHours()) + ":"
			+ pad2(dt.getUTCMinutes()) + ":" + pad2(dt.getUTCSeconds());
}

function pad2(number) {
	var text = "" + number;
	if (text.length == 0) {
		return "00";
	} else if (text.length == 1) {
		return "0" + text;
	} else {
		return text;
	}
}
